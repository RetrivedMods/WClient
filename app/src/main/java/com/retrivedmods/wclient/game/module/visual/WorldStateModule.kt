package com.retrivedmods.wclient.game.module.visual

import android.annotation.SuppressLint
import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.ActionBarManager
import org.cloudburstmc.protocol.bedrock.packet.*

class WorldStateModule : Module("world_state", ModuleCategory.Visual) {

    private val showEntities by boolValue("show_entities", true)
    private val showPlayers by boolValue("show_players", true)
    private val showTime by boolValue("show_time", true)
    private val showChunks by boolValue("show_chunks", true)
    private val coloredText by boolValue("colored_text", true)
    private val updateInterval by intValue("update_interval", 500, 100..2000)

    private var lastDisplayTime = 0L
    private var loadedChunks = 0
    private var worldTime = 0L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        val packet = interceptablePacket.packet

        if (packet is UpdateBlockPacket) {
            loadedChunks++
        }

        if (packet is SetTimePacket) {
            worldTime = packet.time.toLong()
        }

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastDisplayTime < updateInterval) {
            return
        }
        lastDisplayTime = currentTime

        if (isEnabled && isSessionCreated) {
            val text = buildString {
                if (showEntities) {
                    append(if (coloredText) "§aEntities: " else "Entities: ")
                    append(session.level.entityMap.size)
                }

                if (showPlayers) {
                    if (showEntities) append(if (coloredText) " §7| " else " | ")
                    append(if (coloredText) "§cPlayers: " else "Players: ")
                    append(session.level.playerMap.size)
                }

                if (showTime) {
                    if (showEntities || showPlayers) append(if (coloredText) " §7| " else " | ")
                    append(if (coloredText) "§eTime: " else "Time: ")
                    append(formatMinecraftTime(worldTime))
                }

                if (showChunks) {
                    if (showEntities || showPlayers || showTime) append(if (coloredText) " §7| " else " | ")
                    append(if (coloredText) "§dChunks: " else "Chunks: ")
                    append(loadedChunks)
                }
            }

            ActionBarManager.updateModule("worldstate", text)
            ActionBarManager.display(session)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun formatMinecraftTime(ticks: Long): String {
        val hours = ((ticks / 1000 + 6) % 24)
        val minutes = ((ticks % 1000) * 60 / 1000)
        return String.format("%02d:%02d", hours, minutes)
    }

    override fun onEnabled() {
        super.onEnabled()
        loadedChunks = 0
        worldTime = 0
    }

    override fun onDisabled() {
        super.onDisabled()
        if (isSessionCreated) {
            ActionBarManager.removeModule("worldstate")
            ActionBarManager.display(session)
        }
    }
}