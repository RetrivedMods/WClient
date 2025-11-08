package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import org.cloudburstmc.protocol.bedrock.packet.TextPacket
import kotlin.math.atan2
import kotlin.math.sqrt

class BaritoneModule : Module("baritone", ModuleCategory.Misc) {

    private var targetX = 0.0
    private var targetY = 0.0
    private var targetZ = 0.0
    private var isPathing = false
    private var lastJumpTime = 0L
    private val jumpCooldown = 500

    private val walkSpeed by floatValue("speed", 0.6f, 0.1f..2.0f)
    private val jumpHeight by floatValue("jump_height", 0.42f, 0.1f..1.0f)
    private val distanceThreshold by floatValue("completion_distance", 1.0f, 0.5f..5.0f)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        val packet = interceptablePacket.packet

        if (packet is TextPacket && packet.type == TextPacket.Type.CHAT) {
            val message = packet.message
            if (message.startsWith(".goto")) {
                interceptablePacket.intercept()
                handleGotoCommand(message as String)
                return
            }
        }

        if (!isEnabled || !isPathing) return

        if (packet is PlayerAuthInputPacket) {
            val player = session.localPlayer
            val currentPos = player.vec3Position

            val distance = sqrt(
                (currentPos.x - targetX) * (currentPos.x - targetX) +
                        (currentPos.y - targetY) * (currentPos.y - targetY) +
                        (currentPos.z - targetZ) * (currentPos.z - targetZ)
            )

            if (distance <= distanceThreshold) {
                isPathing = false
                session.displayClientMessage("§l§b[Baritone] §r§aDestination reached!")
                return
            }

            val dx = targetX - currentPos.x
            val dz = targetZ - currentPos.z
            val angle = -Math.toDegrees(atan2(dx, dz)).toFloat()

            packet.rotation = Vector3f.from(
                player.rotationPitch,
                angle,
                player.rotationYaw
            )

            val motionX = -Math.sin(Math.toRadians(angle.toDouble())) * walkSpeed
            val motionZ = Math.cos(Math.toRadians(angle.toDouble())) * walkSpeed

            val shouldJump = packet.inputData.contains(PlayerAuthInputData.VERTICAL_COLLISION) &&
                    currentPos.y < targetY &&
                    kotlin.math.abs(currentPos.y - targetY) > 0.5 &&
                    System.currentTimeMillis() - lastJumpTime >= jumpCooldown

            val motionY = if (shouldJump) {
                lastJumpTime = System.currentTimeMillis()
                jumpHeight
            } else {
                if (packet.inputData.contains(PlayerAuthInputData.VERTICAL_COLLISION)) {
                    0f
                } else {
                    -0.08f
                }
            }

            val motionPacket = SetEntityMotionPacket().apply {
                runtimeEntityId = player.runtimeEntityId
                motion = Vector3f.from(
                    motionX.toFloat(),
                    motionY,
                    motionZ.toFloat()
                )
            }
            session.clientBound(motionPacket)
        }
    }

    fun handleGotoCommand(message: String) {
        val args = message.split(" ")
        if (args.size != 4) {
            session.displayClientMessage("§l§b[Baritone] §r§cUsage: .goto <x> <y> <z>")
            return
        }

        try {
            targetX = args[1].toDouble()
            targetY = args[2].toDouble()
            targetZ = args[3].toDouble()
            isPathing = true
            isEnabled = true

            session.displayClientMessage(
                "§l§b[Baritone] §r§7Walking to §f${targetX.toInt()}" +
                        " ${targetY.toInt()} ${targetZ.toInt()}"
            )
        } catch (e: NumberFormatException) {
            session.displayClientMessage("§l§b[Baritone] §r§cInvalid coordinates")
        }
    }
}