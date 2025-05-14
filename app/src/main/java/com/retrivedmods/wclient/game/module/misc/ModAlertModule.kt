package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.AddPlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.RemoveEntityPacket
import android.util.Log

class ModAlertModule : Module("Mod Alert", ModuleCategory.Visual) {

    private val trackedPlayers = mutableMapOf<Long, String>()

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet

        when (packet) {
            is AddPlayerPacket -> {
                // Log the incoming packet to verify it's being captured
                Log.d("ModAlertModule", "AddPlayerPacket: ${packet.username}, EntityId: ${packet.uniqueEntityId}")

                if (packet.uniqueEntityId != session.localPlayer.uniqueEntityId) {
                    val username = packet.username

                    // Check if the username is valid and has not been processed yet
                    if (username.isNotBlank() && trackedPlayers.put(packet.uniqueEntityId, username) == null) {
                        Log.d("ModAlertModule", "Tracking new player: $username")

                        when {
                            isMod(username) -> {
                                Log.d("ModAlertModule", "Mod detected: $username")
                                session.displayClientMessage("§c[!] §aMOD ALERT: §f$username §ahas joined the server!")
                                session.displayClientMessage("§e[!] Disable hacks immediately to avoid bans.")
                            }

                            isVip(username) -> {
                                Log.d("ModAlertModule", "VIP detected: $username")
                                session.displayClientMessage("§d[!] §6VIP ALERT: §f$username §dhas joined the server!")
                            }

                            else -> {
                                Log.d("ModAlertModule", "No Mod or VIP detected: $username")
                            }
                        }
                    }
                }
            }

            is RemoveEntityPacket -> {
                // Remove the player from tracking when they leave
                trackedPlayers.remove(packet.uniqueEntityId)
            }
        }
    }

    private fun isMod(username: String): Boolean {
        // Check for 'Mod' or 'MOD' with optional color codes
        return username.matches(Regex(".*§[0-9a-fk-or]{1,2}(Mod|mod|MOD).*"))
    }

    private fun isVip(username: String): Boolean {
        // Check for 'VIP' with optional color codes
        return username.matches(Regex(".*§[0-9a-fk-or]{1,2}(VIP|vip|Vip).*"))
    }

    fun onDisable() {
        // Clear tracked players when the module is disabled
        trackedPlayers.clear()
    }
}
