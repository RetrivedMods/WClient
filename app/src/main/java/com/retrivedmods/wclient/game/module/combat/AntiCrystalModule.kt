package com.retrivedmods.wclient.game.module.combat


import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class AntiCrystalModule : Module("anti_crystal", ModuleCategory.Combat) {

    private var reduce by floatValue("reduce", 0.6f, 0.1f..1f)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        }

        val actorPos = session.localPlayer.vec3Position
        val newY = actorPos.y - reduce

        val packet = interceptablePacket.packet
        when (packet) {
            is PlayerAuthInputPacket -> {
                packet.position = Vector3f.from(packet.position.x, newY, packet.position.z)
            }

            is MovePlayerPacket -> {
                packet.position = Vector3f.from(packet.position.x, newY, packet.position.z)
            }
        }
    }

}