package com.retrivedmods.wclient.game.module.misc

import android.util.Log
import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cloudburstmc.protocol.bedrock.packet.TextPacket

class SpammerModule : Module("Spammer", ModuleCategory.Misc) {
    companion object {
        private const val TAG = "Spammer"
    }


    private val message by stringValue("Message", "WClient is Best", listOf())
    private val delay by intValue("Delay", 1000, 50..10000)
    private val mode by enumValue("Mode", SpamMode.REPEAT, SpamMode::class.java)
    private val randomize by boolValue("Randomize", false)
    private val antiSpam by boolValue("AntiSpam", false)
    private val antiSpamDelay by intValue("AntiSpam Delay", 3000, 1000..30000)
    private val bypassMode by enumValue("Bypass", BypassMode.NONE, BypassMode::class.java)
    private val prefix by stringValue("Prefix", "", listOf())
    private val suffix by stringValue("Suffix", "", listOf())
    private val messagesList by stringValue("Messages", "WClient is Best|WClient on Top|Best Client", listOf())
    
    enum class BypassMode { NONE, CHAT_SPLIT, UNICODE, INVISIBLE }


    private var isSpamming = false
    private var messageCount = 0
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val randomSuffixes = listOf(
        "!", "!!", "!!!", ".", "..", "...",
        " :D", " :)", " xD", " lol", " gg",
        " ez", " pro", " op", " best"
    )

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {

    }

    override fun onEnabled() {
        super.onEnabled()
        messageCount = 0
        startSpamming()

    }

    override fun onDisabled() {
        super.onDisabled()
        stopSpamming()


        messageCount = 0
    }

    private fun startSpamming() {
        if (isSpamming) return
        isSpamming = true

        coroutineScope.launch {
            Log.d(TAG, "Spammer started - Mode: $mode, Delay: ${delay}ms")

            while (isEnabled && isSpamming) {
                try {
                    if (isSessionCreated) {
                        sendChatMessage()
                        messageCount++
                    }

                    delay(delay.toLong())
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending message", e)
                    delay(1000)
                }
            }

            Log.d(TAG, "Spammer stopped")
        }
    }

    private fun stopSpamming() {
        isSpamming = false
    }

    private fun sendChatMessage() {
        val messageToSend = buildMessage()

        val textPacket = TextPacket()
        textPacket.type = TextPacket.Type.CHAT
        textPacket.sourceName = ""
        textPacket.message = messageToSend
        textPacket.xuid = ""
        textPacket.platformChatId = ""
        textPacket.setNeedsTranslation(false)

        session.serverBound(textPacket)

        Log.d(TAG, "Sent message #$messageCount: \"$messageToSend\"")
    }

    private fun buildMessage(): String {
        var msg = message



        if (randomize) {
            msg = msg + randomSuffixes.random()
        }

        // Apply mode
        msg = when (mode) {
            SpamMode.REPEAT -> msg
            SpamMode.UPPERCASE -> msg.uppercase()
            SpamMode.LOWERCASE -> msg.lowercase()
            SpamMode.ALTERNATING -> alternatingCase(msg)
            SpamMode.REVERSE -> msg.reversed()
        }

        return msg
    }

    private fun alternatingCase(text: String): String {
        return text.mapIndexed { index, char ->
            if (index % 2 == 0) char.uppercase() else char.lowercase()
        }.joinToString("")
    }

    enum class SpamMode {
        REPEAT,
        UPPERCASE,
        LOWERCASE,
        ALTERNATING,
        REVERSE
    }
}
