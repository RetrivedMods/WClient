package com.retrivedmods.wclient.game

import net.kyori.adventure.text.Component
import org.cloudburstmc.protocol.bedrock.packet.SetTitlePacket

object ActionBarManager {
    private val activeModules = mutableMapOf<String, String>()

    fun updateModule(moduleName: String, text: String) {
        if (text.isEmpty()) {
            activeModules.remove(moduleName)
        } else {
            activeModules[moduleName] = text
        }
    }

    fun removeModule(moduleName: String) {
        activeModules.remove(moduleName)
    }

    fun display(session: GameSession) {
        if (activeModules.isEmpty()) return

        val combinedText = activeModules.values.joinToString(" §7|§r ")

        session.clientBound(SetTitlePacket().apply {
            type = SetTitlePacket.Type.ACTIONBAR
            text = combinedText
            fadeInTime = 0
            fadeOutTime = 0
            stayTime = 2
            xuid = ""
            platformOnlineId = ""
        })
    }
}