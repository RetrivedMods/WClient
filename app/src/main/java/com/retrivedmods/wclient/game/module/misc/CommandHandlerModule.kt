package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.ModuleManager
import org.cloudburstmc.protocol.bedrock.packet.TextPacket

class CommandHandlerModule : Module("command_handler", ModuleCategory.Misc, true) {
    private val prefix = "."

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is TextPacket && packet.type == TextPacket.Type.CHAT) {
            val message = packet.message
            if (!message.startsWith(prefix)) return

            interceptablePacket.intercept()

            val args = message.substring(prefix.length).split(" ")
            val command = args[0].lowercase()

            when (command) {
                "help" -> {
                    displayHelp(args.getOrNull(1))
                }
                "goto" -> {
                    val baritoneModule = ModuleManager.modules.find { it is BaritoneModule } as? BaritoneModule
                    if (baritoneModule == null) {
                        session.displayClientMessage("§cBaritone module not found")
                        return
                    }
                    baritoneModule.handleGotoCommand(message as String)
                }
                "replay" -> {
                    val replayModule = ModuleManager.modules.find { it is ReplayModule } as? ReplayModule
                    if (replayModule == null) {
                        session.displayClientMessage("§cReplay module not found")
                        return
                    }

                    when (args.getOrNull(1)?.lowercase()) {
                        "record" -> replayModule.startRecording()
                        "play" -> replayModule.startPlayback()
                        "stop" -> {
                            replayModule.stopRecording()
                            replayModule.stopPlayback()
                        }
                        "save" -> {
                            val name = args.getOrNull(2)
                            if (name == null) {
                                session.displayClientMessage("§cUsage: .replay save <name>")
                                return
                            }
                            replayModule.saveReplay(name)
                        }
                        "load" -> {
                            val name = args.getOrNull(2)
                            if (name == null) {
                                session.displayClientMessage("§cUsage: .replay load <name>")
                                return
                            }
                            replayModule.loadReplay(name)
                        }
                        else -> {
                            session.displayClientMessage("""
                                §l§b[Replay] §r§7Commands:
                                §f.replay record §7- Start recording
                                §f.replay play §7- Play last recording  
                                §f.replay stop §7- Stop recording/playback
                                §f.replay save <name> §7- Save recording
                                §f.replay load <name> §7- Load recording
                            """.trimIndent())
                        }
                    }
                }
                else -> {
                    val module = ModuleManager.modules.find { it.name.equals(command, ignoreCase = true) }
                    if (module != null && !module.private) {
                        module.isEnabled = !module.isEnabled
                    } else {
                        session.displayClientMessage("§l§c[WClient] §r§cModule not found: §f$command")
                    }
                }
            }
        }
    }

    private fun displayHelp(category: String?) {
        val header = """
            §l§c[WClient] §r§7Module List
            §7Commands:
            §f.help <category> §7- View modules in a category
            §f.<module> §7- Toggle a module
            §f.help §7- Show all categories
            §r§7
        """.trimIndent()

        session.displayClientMessage(header)

        if (category != null) {
            try {
                val moduleCategory = ModuleCategory.valueOf(category.uppercase())
                displayCategoryModules(moduleCategory)
            } catch (e: IllegalArgumentException) {
                session.displayClientMessage("§cInvalid category: $category")
                session.displayClientMessage("§7Available categories: ${ModuleCategory.entries.joinToString("§f, §7") { it.name.lowercase() }}")
            }
            return
        }

        ModuleCategory.entries.forEach { cat ->
            displayCategoryModules(cat)
        }
    }

    private fun displayCategoryModules(category: ModuleCategory) {
        val modules = ModuleManager.modules
            .filterNot { it.private }
            .filter { it.category == category }

        if (modules.isEmpty()) return

        session.displayClientMessage("§l§b§m--------------------")
        session.displayClientMessage("§l§b${category.name} Modules:")
        session.displayClientMessage("§r§7")

        modules.chunked(3).forEach { row ->
            val formattedRow = row.joinToString("   ") { module ->
                val status = if (module.isEnabled) "§a✔️" else "§c✘"
                "$status §f${module.name}"
            }
            session.displayClientMessage(formattedRow)
        }

        session.displayClientMessage("§r§7")
    }
}