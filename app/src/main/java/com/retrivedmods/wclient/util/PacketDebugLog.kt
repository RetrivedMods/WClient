package com.retrivedmods.wclient.util

import com.retrivedmods.wclient.game.GameSession
import com.retrivedmods.wclient.util.setPacketField
import org.cloudburstmc.protocol.bedrock.packet.TextPacket

/**
 * Tiny shared hook so code that sends packets directly via session.serverBound(...) (bypassing
 * the beforePacketBound intercept pipeline PacketLoggerModule listens on, e.g.
 * LocalPlayer.placeBlock) can still get logged to chat when that module is enabled. Toggled by
 * PacketLoggerModule.onEnabled/onDisabled - nothing else should touch [enabled].
 */
object PacketDebugLog {

    @Volatile
    var enabled: Boolean = false

    fun log(session: GameSession, tag: String, body: String) {
        if (!enabled) return
        val textPacket = TextPacket().apply {
            type = TextPacket.Type.RAW
            setPacketField("needsTranslation", false)
            setPacketField("message", "§l§b[$tag]§r\n$body")
            xuid = ""
            sourceName = ""
        }
        session.clientBound(textPacket)
    }
}
