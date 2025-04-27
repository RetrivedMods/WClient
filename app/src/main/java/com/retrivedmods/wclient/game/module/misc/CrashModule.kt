package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3i
import org.cloudburstmc.protocol.bedrock.packet.SubChunkRequestPacket

class CrashModule : Module("crash", ModuleCategory.Misc) {
    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        } else {
            val subChunkRequestPacket = SubChunkRequestPacket().apply {
                dimension = 0
                subChunkPosition = Vector3i.ZERO
                positionOffsets = Array(3000000) { Vector3i.ZERO }.toMutableList()
            }

            session.clientBound(subChunkRequestPacket)
        }
    }
}