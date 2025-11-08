package com.retrivedmods.wclient.desktop.core

import com.radiantbyte.novarelay.NovaRelay
import com.radiantbyte.novarelay.address.NovaAddress
import com.radiantbyte.novarelay.config.EnhancedServerConfig
import com.radiantbyte.novarelay.definition.Definitions
import com.radiantbyte.novarelay.listener.AutoCodecPacketListener
import com.radiantbyte.novarelay.listener.GamingPacketHandler
import com.radiantbyte.novarelay.listener.OnlineLoginPacketListener
import net.raphimc.minecraftauth.step.bedrock.session.StepFullBedrockSession
import kotlin.concurrent.thread

object RelayService {
    
    private var relay: NovaRelay? = null
    private var relayThread: Thread? = null
    private var statusCallback: ((String) -> Unit)? = null
    private var consoleCallback: ((String) -> Unit)? = null
    
    init {
        try {
            Definitions.loadBlockPalette()
            log("Block palette loaded")
        } catch (e: Exception) {
            log("Failed to load block palette: ${e.message}")
        }
    }
    
    fun setStatusCallback(callback: (String) -> Unit) {
        statusCallback = callback
    }
    
    fun setConsoleCallback(callback: (String) -> Unit) {
        consoleCallback = callback
    }
    
    fun startRelay(
        serverHost: String,
        serverPort: Int,
        localPort: Int,
        account: StepFullBedrockSession.FullBedrockSession
    ) {
        if (relay != null) {
            throw IllegalStateException("Relay is already running")
        }
        
        relayThread = thread(name = "NovaRelayThread") {
            try {
                log("Starting NovaRelay...")
                log("Server: $serverHost:$serverPort")
                log("Local: 0.0.0.0:$localPort (accessible via localhost or LAN)")
                log("Account: ${account.mcChain.displayName}")
                
                val remoteAddress = NovaAddress(serverHost, serverPort)
                val localAddress = NovaAddress("0.0.0.0", localPort)
                
                relay = NovaRelay(
                    localAddress = localAddress,
                    serverConfig = EnhancedServerConfig.DEFAULT
                ).capture(remoteAddress = remoteAddress) {
                    log("Session created - Client connected!")
                    
                    listeners.add(AutoCodecPacketListener(this))
                    listeners.add(OnlineLoginPacketListener(this, account))
                    listeners.add(GamingPacketHandler(this))
                    listeners.add(DesktopCommandHandler(this))
                    
                    log("Listeners registered")
                    log("Waiting for client to authenticate...")
                }
                
                updateStatus("Running on 0.0.0.0:$localPort -> $serverHost:$serverPort")
                log("NovaRelay started successfully")
                log("Listening on: 0.0.0.0:$localPort")
                log("Target server: $serverHost:$serverPort")
                log("Windows PC: Connect to localhost:$localPort")
                log("Mobile: Use LAN discovery")
                log("Waiting for Minecraft to connect...")
                
            } catch (e: Exception) {
                log("Failed to start relay: ${e.message}")
                e.printStackTrace()
                relay = null
            }
        }
    }
    
    fun stopRelay() {
        try {
            log("Stopping NovaRelay...")
            
            relay?.let { r ->
                r.novaRelaySession?.client?.disconnect()
                r.novaRelaySession?.server?.disconnect()
                r.stop()
            }
            
            relay = null
            relayThread?.interrupt()
            relayThread = null
            
            updateStatus("Stopped")
            log("NovaRelay stopped")
        } catch (e: Exception) {
            log("Error stopping relay: ${e.message}")
        }
    }
    
    fun isRunning(): Boolean {
        return relay != null
    }
    
    private fun updateStatus(status: String) {
        statusCallback?.invoke(status)
    }
    
    private fun log(message: String) {
        println(message)
        consoleCallback?.invoke(message)
    }
}
