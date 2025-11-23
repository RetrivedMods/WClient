package com.retrivedmods.wclient.desktop.core

import com.retrivedmods.wrelay.listener.WRelayPacketListener
import com.retrivedmods.wrelay.WRelaySession
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.TextPacket

class DesktopCommandHandler(
    private val session: WRelaySession
) : WRelayPacketListener {
    
    private val prefix = "."
    
    override fun beforeClientBound(packet: BedrockPacket): Boolean {
        if (packet is TextPacket && packet.type == TextPacket.Type.CHAT) {
            val message = packet.message
            if (!message.startsWith(prefix)) return false
            
            val args = message.substring(prefix.length).split(" ")
            val command = args[0].lowercase()
            
            when (command) {
                "help" -> {
                    displayHelp(args.getOrNull(1))
                    return true
                }
                else -> {
                    if (DesktopModuleManager.getModules().any { it.name.equals(command, ignoreCase = true) }) {
                        DesktopModuleManager.toggleModule(command)
                        val enabled = DesktopModuleManager.isModuleEnabled(command)
                        sendMessage("§l§b[NovaClient] §r§f$command: ${if (enabled) "§aEnabled" else "§cDisabled"}")
                        return true
                    } else {
                        sendMessage("§l§b[NovaClient] §r§cModule not found: §f$command")
                        return true
                    }
                }
            }
        }
        return false
    }
    
    private fun displayHelp(category: String?) {
        val header = """
            §l§b[NovaClient Desktop] §r§7v1.9.1
            §7Commands:
            §f.help <category> §7- View modules in a category
            §f.<module> §7- Toggle a module
            §r§7
        """.trimIndent()
        
        sendMessage(header)
        
        if (category != null) {
            displayCategoryModules(category)
            return
        }
        
        val categories = DesktopModuleManager.getModules()
            .map { it.category }
            .distinct()
        
        categories.forEach { cat ->
            displayCategoryModules(cat)
        }
    }
    
    private fun displayCategoryModules(category: String) {
        val modules = DesktopModuleManager.getModules()
            .filter { it.category.equals(category, ignoreCase = true) }
        
        if (modules.isEmpty()) return
        
        sendMessage("§l§b§m--------------------")
        sendMessage("§l§b$category Modules:")
        sendMessage("§r§7")
        
        modules.chunked(3).forEach { row ->
            val formattedRow = row.joinToString("   ") { module ->
                val status = if (module.enabled) "§a✔" else "§c✘"
                "$status §f${module.name}"
            }
            sendMessage(formattedRow)
        }
        
        sendMessage("§r§7")
    }
    
    private fun sendMessage(message: String) {
        val textPacket = TextPacket()
        textPacket.type = TextPacket.Type.RAW
        textPacket.sourceName = ""
        textPacket.message = message
        textPacket.xuid = ""
        textPacket.platformChatId = ""
        textPacket.filteredMessage = ""
        session.clientBound(textPacket)
    }
}
