package com.retrivedmods.wclient.game.module.player

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.random.Random

class DesyncModule : Module("desync", ModuleCategory.Player) {

    private var isDesynced = false
    private val storedPackets = ConcurrentLinkedQueue<PlayerAuthInputPacket>()
    private val updateDelay = 1000L
    private val minResendInterval = 100L
    private val maxResendInterval = 300L

    override fun onEnabled() {
        super.onEnabled()
        isDesynced = true
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onDisabled() {
        super.onDisabled()
        isDesynced = false

        GlobalScope.launch {
            delay(updateDelay)
            while (storedPackets.isNotEmpty()) {
                val packet = storedPackets.poll()
                if (packet != null) {
                    session.clientBound(packet)
                }
                delay(Random.Default.nextLong(minResendInterval, maxResendInterval))
            }
        }
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled || !isDesynced) {
            return
        }

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            storedPackets.add(packet)
            interceptablePacket.intercept()
        }
    }

}