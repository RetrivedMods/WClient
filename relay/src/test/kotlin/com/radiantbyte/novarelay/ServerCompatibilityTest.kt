package com.radiantbyte.novarelay

import com.radiantbyte.novarelay.address.NovaAddress
import com.radiantbyte.novarelay.config.EnhancedServerConfig
import com.radiantbyte.novarelay.util.ServerCompatUtils
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ServerCompatibilityTest {

    @Test
    fun `test protected server detection`() {
        assertTrue(ServerCompatUtils.isProtectedHostname("server123.aternos.me"))
        assertTrue(ServerCompatUtils.isProtectedHostname("myserver.aternos.me"))
        assertTrue(ServerCompatUtils.isProtectedHostname("test.aternos.org"))
        assertTrue(ServerCompatUtils.isProtectedHostname("example.aternos.net"))

        assertFalse(ServerCompatUtils.isProtectedHostname("play.lbsg.net"))
        assertFalse(ServerCompatUtils.isProtectedHostname("mineplex.com"))
        assertFalse(ServerCompatUtils.isProtectedHostname("localhost"))
    }
    
    @Test
    fun `test protected server info extraction`() {
        val numericServerInfo = ServerCompatUtils.extractServerInfo("123456.aternos.me")
        assertEquals("123456", numericServerInfo?.serverId)
        assertEquals("me", numericServerInfo?.domain)
        assertTrue(numericServerInfo?.isNumericId ?: false)

        val serverInfo = ServerCompatUtils.extractServerInfo("server123.aternos.me")
        assertEquals("server123", serverInfo?.serverId)
        assertEquals("me", serverInfo?.domain)
        assertFalse(serverInfo?.isNumericId ?: true)

        val customServerInfo = ServerCompatUtils.extractServerInfo("myserver.aternos.me")
        assertEquals("myserver", customServerInfo?.serverId)
        assertEquals("me", customServerInfo?.domain)
        assertFalse(customServerInfo?.isNumericId ?: true)
    }
    
    @Test
    fun `test server configuration selection`() {
        val numericServer = NovaAddress("123456.aternos.me", 19132)
        val customServer = NovaAddress("myserver.aternos.me", 19132)
        val regularServer = NovaAddress("play.lbsg.net", 19132)

        val numericConfig = ServerCompatUtils.getRecommendedConfig(numericServer)
        val customConfig = ServerCompatUtils.getRecommendedConfig(customServer)
        val regularConfig = ServerCompatUtils.getRecommendedConfig(regularServer)

        assertEquals(EnhancedServerConfig.DEFAULT.maxRetryAttempts, numericConfig.maxRetryAttempts)

        assertTrue(customConfig.maxRetryAttempts <= EnhancedServerConfig.DEFAULT.maxRetryAttempts)

        assertEquals(EnhancedServerConfig.FAST.maxRetryAttempts, regularConfig.maxRetryAttempts)
    }
    
    @Test
    fun `test connection retry delay calculation`() {
        val config = EnhancedServerConfig.DEFAULT

        val delay1 = config.calculateRetryDelay(0)
        val delay2 = config.calculateRetryDelay(1)
        val delay3 = config.calculateRetryDelay(2)
        
        assertTrue(delay1 <= delay2)
        assertTrue(delay2 <= delay3)
        assertTrue(delay3 <= config.maxRetryDelay)
    }
    
    @Test
    fun `test NovaRelay with enhanced configuration`() {
        val protectedAddress = NovaAddress("play.lbsg.net", 19132)
        val relay = NovaRelay(
            localAddress = NovaAddress("127.0.0.1", 19133),
            serverConfig = EnhancedServerConfig.FAST
        )

        relay.capture(protectedAddress) {
            println("Session created for protected server")
        }

        assertTrue(relay.isRunning)
        assertEquals(protectedAddress, relay.remoteAddress)
    }
    
    @Test
    fun `test connection manager initialization`() {
        val protectedAddress = NovaAddress("server456.aternos.me", 19132)
        val relay = NovaRelay(serverConfig = EnhancedServerConfig.AGGRESSIVE)

        relay.capture(protectedAddress) {
        }

        assertTrue(ServerCompatUtils.isProtectedServer(protectedAddress))
    }

    // @Test
    fun `manual test - connect to real protected server`() = runBlocking {
        val protectedServer = NovaAddress("play.lbsg.net", 63516)

        val relay = NovaRelay(
            localAddress = NovaAddress("127.0.0.1", 19133),
            serverConfig = EnhancedServerConfig.AGGRESSIVE
        )
        
        var connectionSuccessful = false
        
        relay.capture(protectedServer) {
            listeners.add(object : com.radiantbyte.novarelay.listener.NovaRelayPacketListener {
                override fun onDisconnect(reason: String) {
                    println("Disconnected from protected server: $reason")
                }
            })

            try {
                runBlocking {
                    withTimeout(30000) {
                        val result = relay.connectToServerAsync {
                            println("Successfully connected to protected server!")
                            connectionSuccessful = true
                        }

                        if (result.isSuccess) {
                            println("Connection established successfully")
                        } else {
                            println("Connection failed: ${result.exceptionOrNull()?.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                println("Connection test failed: ${e.message}")
            }
        }

        runBlocking {
            kotlinx.coroutines.delay(5000)
        }

        println("Connection test completed. Success: $connectionSuccessful")
    }

    @Test
    fun `test connection tips generation`() {
        val protectedServer = NovaAddress("play.cubecraft.net", 19132)
        val regularServer = NovaAddress("play.lbsg.net", 19132)

        val protectedTips = ServerCompatUtils.getConnectionTips(protectedServer)
        val regularTips = ServerCompatUtils.getConnectionTips(regularServer)

        assertTrue(protectedTips.isNotEmpty())
        assertTrue(regularTips.isEmpty())

        assertTrue(protectedTips.any { it.contains("Protected") })
        assertTrue(protectedTips.any { it.contains("DDoS protection") })
    }
}