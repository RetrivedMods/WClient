package com.retrivedmods.wrelay.connection

import com.retrivedmods.wrelay.WRelaySession
import com.retrivedmods.wrelay.address.WAddress
import com.retrivedmods.wrelay.address.inetSocketAddress
import com.retrivedmods.wrelay.client.ClientIdentification
import com.retrivedmods.wrelay.config.EnhancedServerConfig
import com.retrivedmods.wrelay.util.ServerCompatUtils
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioDatagramChannel
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import org.cloudburstmc.netty.channel.raknet.RakChannelFactory
import org.cloudburstmc.netty.channel.raknet.config.RakChannelOption
import org.cloudburstmc.protocol.bedrock.BedrockPeer
import org.cloudburstmc.protocol.bedrock.PacketDirection
import org.cloudburstmc.protocol.bedrock.netty.initializer.BedrockChannelInitializer
import kotlin.random.Random

class ConnectionManager(
    private val wRelaySession: WRelaySession,
    private val serverConfig: EnhancedServerConfig = EnhancedServerConfig.DEFAULT
) {

    private val connectionAttempts = mutableMapOf<String, Long>()
    private val connectionCounts = mutableMapOf<String, Int>()
    private val rateLimitResetTime = mutableMapOf<String, Long>()
    private var isConnecting = false
    private var eventLoopGroup: NioEventLoopGroup? = null

    companion object {
        private const val RATE_LIMIT_WINDOW_MS = 60000L
        private const val MAX_CONNECTIONS_PER_WINDOW = 3
    }

    fun cleanup() {
        eventLoopGroup?.shutdownGracefully()
        eventLoopGroup = null
        connectionAttempts.clear()
        connectionCounts.clear()
        rateLimitResetTime.clear()
        isConnecting = false
    }

    suspend fun connectToServer(
        remoteAddress: WAddress,
        onSessionCreated: WRelaySession.ClientSession.() -> Unit
    ): Result<WRelaySession.ClientSession> = withContext(Dispatchers.IO) {
        
        if (isConnecting) {
            return@withContext Result.failure(IllegalStateException("Already connecting to a server"))
        }
        
        isConnecting = true
        
        try {
            val isProtected = ServerCompatUtils.isProtectedServer(remoteAddress)
            val config = if (isProtected) serverConfig else EnhancedServerConfig.FAST
            
            println("Connecting to ${remoteAddress.hostName}:${remoteAddress.port} (Protected: $isProtected)")

            if (isProtected && config.enableConnectionThrottling) {
                applyConnectionThrottling(remoteAddress.hostName, config)
                applyRateLimiting(remoteAddress.hostName)
            }

            if (config.initialConnectionDelay > 0) {
                delay(config.initialConnectionDelay)
            }
            
            var lastException: Exception? = null
            
            for (attempt in 0 until config.maxRetryAttempts) {
                try {
                    println("Connection attempt ${attempt + 1}/${config.maxRetryAttempts}")
                    
                    val clientSession = attemptConnection(remoteAddress, config, onSessionCreated)
                    println("Successfully connected to ${remoteAddress.hostName}:${remoteAddress.port}")
                    return@withContext Result.success(clientSession)
                    
                } catch (e: Exception) {
                    lastException = e
                    println("Connection attempt ${attempt + 1} failed: ${e.message}")

                    if (shouldNotRetry(e)) {
                        break
                    }

                    if (attempt < config.maxRetryAttempts - 1) {
                        val retryDelay = config.calculateRetryDelay(attempt)
                        println("Retrying in ${retryDelay}ms...")
                        delay(retryDelay)
                    }
                }
            }
            
            Result.failure(lastException ?: Exception("Connection failed after ${config.maxRetryAttempts} attempts"))
            
        } finally {
            isConnecting = false
        }
    }

    private suspend fun applyConnectionThrottling(hostname: String, config: EnhancedServerConfig) {
        val lastAttempt = connectionAttempts[hostname]
        if (lastAttempt != null) {
            val timeSinceLastAttempt = System.currentTimeMillis() - lastAttempt
            if (timeSinceLastAttempt < config.timeBetweenConnectionAttempts) {
                val waitTime = config.timeBetweenConnectionAttempts - timeSinceLastAttempt
                println("Throttling connection to $hostname, waiting ${waitTime}ms...")
                delay(waitTime)
            }
        }
        connectionAttempts[hostname] = System.currentTimeMillis()
    }

    private suspend fun applyRateLimiting(hostname: String) {
        val currentTime = System.currentTimeMillis()
        val resetTime = rateLimitResetTime[hostname] ?: 0L

        if (currentTime > resetTime) {
            connectionCounts[hostname] = 0
            rateLimitResetTime[hostname] = currentTime + RATE_LIMIT_WINDOW_MS
        }

        val currentCount = connectionCounts[hostname] ?: 0

        if (currentCount >= MAX_CONNECTIONS_PER_WINDOW) {
            val waitTime = rateLimitResetTime[hostname]!! - currentTime
            if (waitTime > 0) {
                println("Rate limit exceeded for $hostname, waiting ${waitTime}ms...")
                delay(waitTime)
                connectionCounts[hostname] = 0
                rateLimitResetTime[hostname] = System.currentTimeMillis() + RATE_LIMIT_WINDOW_MS
            }
        }

        connectionCounts[hostname] = currentCount + 1
    }

    private suspend fun attemptConnection(
        remoteAddress: WAddress,
        config: EnhancedServerConfig,
        onSessionCreated: WRelaySession.ClientSession.() -> Unit
    ): WRelaySession.ClientSession = suspendCancellableCoroutine { continuation ->

        val isProtected = ServerCompatUtils.isProtectedServer(remoteAddress)
        val clientConfig = if (isProtected) {
            ClientIdentification.getEnhancedClientConfig()
        } else {
            ClientIdentification.getStandardClientConfig()
        }

        if (clientConfig.useRealisticTiming) {
            val delay = ClientIdentification.getRealisticConnectionDelay()
            println("Adding realistic connection delay: ${delay}ms")
            CoroutineScope(Dispatchers.IO).launch {
                delay(delay)
            }
        }

        if (eventLoopGroup == null || eventLoopGroup!!.isShuttingDown || eventLoopGroup!!.isShutdown) {
            eventLoopGroup = NioEventLoopGroup()
        }
        
        val bootstrap = Bootstrap()
            .group(eventLoopGroup)
            .channelFactory(RakChannelFactory.client(NioDatagramChannel::class.java))
            .option(RakChannelOption.RAK_PROTOCOL_VERSION, clientConfig.protocolVersion)
            .option(RakChannelOption.RAK_GUID, clientConfig.guid)
            .option(RakChannelOption.RAK_CONNECT_TIMEOUT, config.connectionTimeout)
            .option(RakChannelOption.RAK_SESSION_TIMEOUT, if (clientConfig.useRealisticTiming) ClientIdentification.getRealisticSessionTimeout() else config.sessionTimeout)
            .option(RakChannelOption.RAK_COMPATIBILITY_MODE, clientConfig.compatibilityMode)
            .option(RakChannelOption.RAK_UNCONNECTED_MAGIC, clientConfig.unconnectedMagic)
            .option(RakChannelOption.RAK_MTU, 1400)
            .handler(object : BedrockChannelInitializer<WRelaySession.ClientSession>() {
                
                override fun createSession0(peer: BedrockPeer, subClientId: Int): WRelaySession.ClientSession {
                    return wRelaySession.ClientSession(peer, subClientId)
                }
                
                override fun initSession(clientSession: WRelaySession.ClientSession) {
                    wRelaySession.client = clientSession
                    if (!continuation.isCompleted) {
                        continuation.resume(clientSession) {}
                    }
                    onSessionCreated(clientSession)
                }
                
                override fun preInitChannel(channel: Channel) {
                    channel.attr(PacketDirection.ATTRIBUTE).set(PacketDirection.SERVER_BOUND)
                    super.preInitChannel(channel)
                }
            })
            .remoteAddress(remoteAddress.inetSocketAddress)
        
        val connectFuture = bootstrap.connect()

        val timeoutJob = CoroutineScope(Dispatchers.IO).launch {
            delay(config.connectionTimeout + 10000)
            if (!continuation.isCompleted) {
                connectFuture.cancel(true)
                continuation.resumeWithException(Exception("Connection timeout after ${config.connectionTimeout}ms"))
            }
        }
        
        connectFuture.addListener { future ->
            timeoutJob.cancel()
            if (!future.isSuccess && !continuation.isCompleted) {
                continuation.resumeWithException(
                    future.cause() ?: Exception("Connection failed for unknown reason")
                )
            }
        }
        
        continuation.invokeOnCancellation {
            timeoutJob.cancel()
            connectFuture.cancel(true)
        }
    }

    private fun shouldNotRetry(exception: Exception): Boolean {
        val message = exception.message?.lowercase() ?: ""
        return when {
            message.contains("incompatible") -> true
            message.contains("already connected") -> true
            message.contains("no free incoming connections") -> false
            message.contains("connection request failed") -> false
            message.contains("connection refused") -> false
            message.contains("timeout") -> false
            else -> false
        }
    }
}