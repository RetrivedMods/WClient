package com.retrivedmods.wclient.game.module.motion

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket

class BhopModule : Module("bhop", ModuleCategory.Motion) {

    private val jumpHeight by floatValue("jumpHeight", 0.42f, 0.4f..3.0f)
    private val motionInterval by intValue("motionInterval", 120, 50..2000)
    private val times by intValue("times", 1, 1..20)
    private var lastMotionTime = 0L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {

        val packet = interceptablePacket.packet

        if (!isEnabled) {
            return
        }

        val currentTime = System.currentTimeMillis()

        // Apply vertical motion adjustments at defined intervals
        if (currentTime - lastMotionTime >= motionInterval) {
            // Apply upward and downward motion to simulate jumping


            if (packet is PlayerAuthInputPacket) {

                if (packet.inputData.contains(PlayerAuthInputData.VERTICAL_COLLISION)) {


                    val motionPacket = SetEntityMotionPacket().apply {
                        runtimeEntityId = session.localPlayer.runtimeEntityId

                        // Alternate vertical motion to simulate jumping up and down
                        motion = Vector3f.from(
                            session.localPlayer.motionX,  // Keep horizontal motion
                            if ((currentTime / (motionInterval / times)) % 2 == 0L) jumpHeight else -jumpHeight,  // Alternate between upwards and downwards motion
                            session.localPlayer.motionZ   // Keep horizontal motion
                        )
                    }


                    // Send the motion packet to the server
                    session.clientBound(motionPacket)
                }
            }

            // Update the last motion time
            lastMotionTime = currentTime
        }
    }
}