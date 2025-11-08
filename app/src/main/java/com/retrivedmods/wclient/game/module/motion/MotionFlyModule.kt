package com.retrivedmods.wclient.game.module.motion

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.Ability
import org.cloudburstmc.protocol.bedrock.data.AbilityLayer
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData
import org.cloudburstmc.protocol.bedrock.data.PlayerPermission
import org.cloudburstmc.protocol.bedrock.data.command.CommandPermission
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import org.cloudburstmc.protocol.bedrock.packet.UpdateAbilitiesPacket
import kotlin.math.cos
import kotlin.math.sin

class MotionFlyModule : Module("MotionFly", ModuleCategory.Motion) {

    private val horizontalSpeed by floatValue("Horizontal Speed", 3.5f, 0.5f..10.0f)
    private val verticalSpeed by floatValue("Vertical Speed", 1.5f, 0.5f..5.0f)
    private val glideSpeed by floatValue("Glide Speed", 0.1f, -0.01f..1.0f)
    private val bypassMode by boolValue("Lifeboat Bypass", true)
    private val motionInterval by floatValue("Delay", 50.0f, 10.0f..100.0f)

    private var lastMotionTime = 0L
    private var jitterState = false
    private var canFly = false

    private val flyPacket = UpdateAbilitiesPacket().apply {
        playerPermission = PlayerPermission.OPERATOR
        commandPermission = CommandPermission.OWNER
        uniqueEntityId = -1
        abilityLayers.add(AbilityLayer().apply {
            layerType = AbilityLayer.Type.BASE
            abilitiesSet.addAll(Ability.entries.toTypedArray())
            abilityValues.addAll(
                arrayOf(
                    Ability.BUILD,
                    Ability.MINE,
                    Ability.DOORS_AND_SWITCHES,
                    Ability.OPEN_CONTAINERS,
                    Ability.ATTACK_PLAYERS,
                    Ability.ATTACK_MOBS,
                    Ability.OPERATOR_COMMANDS,
                    Ability.MAY_FLY,
                    Ability.FLY_SPEED,
                    Ability.WALK_SPEED
                )
            )
            walkSpeed = 0.1f
            flySpeed = 0.5f
        })
    }

    private val resetPacket = UpdateAbilitiesPacket().apply {
        playerPermission = PlayerPermission.VISITOR
        commandPermission = CommandPermission.ANY
        uniqueEntityId = -1
        abilityLayers.add(AbilityLayer().apply {
            layerType = AbilityLayer.Type.BASE
            abilitiesSet.addAll(Ability.entries.toTypedArray())
            abilityValues.addAll(
                arrayOf(
                    Ability.BUILD,
                    Ability.MINE,
                    Ability.DOORS_AND_SWITCHES,
                    Ability.OPEN_CONTAINERS,
                    Ability.ATTACK_PLAYERS,
                    Ability.ATTACK_MOBS,
                    Ability.OPERATOR_COMMANDS,
                    Ability.FLY_SPEED,
                    Ability.WALK_SPEED
                )
            )
            walkSpeed = 0.1f
            flySpeed = 0.05f
        })
    }

    private fun applyFlyAbilities(enabled: Boolean) {
        if (canFly != enabled) {
            val id = session.localPlayer.uniqueEntityId
            flyPacket.uniqueEntityId = id
            resetPacket.uniqueEntityId = id
            session.clientBound(if (enabled) flyPacket else resetPacket)
            canFly = enabled
        }
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        val packet = interceptablePacket.packet
        if (!isEnabled || packet !is PlayerAuthInputPacket) return

        applyFlyAbilities(true)

        val now = System.currentTimeMillis()
        if (now - lastMotionTime < motionInterval) return

        val vertical = when {
            packet.inputData.contains(PlayerAuthInputData.WANT_UP) -> verticalSpeed
            packet.inputData.contains(PlayerAuthInputData.WANT_DOWN) -> -verticalSpeed
            bypassMode -> -glideSpeed.coerceAtLeast(-0.1f)
            else -> glideSpeed
        }

        // PlayerAuthInputPacket.motion encodes (strafe, forward)
        val inputX = packet.motion.x
        val inputZ = packet.motion.y

        val yawRad = Math.toRadians(packet.rotation.y.toDouble()).toFloat()
        val sinYaw = sin(yawRad)
        val cosYaw = cos(yawRad)

        val strafe = inputX * horizontalSpeed
        val forward = inputZ * horizontalSpeed

        val motionX = strafe * cosYaw - forward * sinYaw
        val motionZ = forward * cosYaw + strafe * sinYaw

        val motionPacket = SetEntityMotionPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            motion = Vector3f.from(
                motionX,
                vertical + if (jitterState) 0.05f else -0.05f,
                motionZ
            )
        }

        session.clientBound(motionPacket)
        jitterState = !jitterState
        lastMotionTime = now
    }

    override fun onDisabled() {
        applyFlyAbilities(false)
    }
}
