package com.retrivedmods.wclient.game.module.world

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.Ability
import org.cloudburstmc.protocol.bedrock.data.AbilityLayer
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData
import org.cloudburstmc.protocol.bedrock.data.PlayerPermission
import org.cloudburstmc.protocol.bedrock.data.command.CommandPermission
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import org.cloudburstmc.protocol.bedrock.packet.TextPacket
import org.cloudburstmc.protocol.bedrock.packet.UpdateAbilitiesPacket
import kotlin.collections.addAll

class FreeCameraModule : Module("free_camera", ModuleCategory.World) {

    private var originalPosition: Vector3f? = null
    private val flySpeed by floatValue("speed", 0.15f, 0.1f..1.5f) // Add configurable speed

    private val enableFlyNoClipPacket = UpdateAbilitiesPacket().apply {
        playerPermission = PlayerPermission.OPERATOR
        commandPermission = CommandPermission.OWNER
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
                    Ability.MAY_FLY,
                    Ability.FLY_SPEED,
                    Ability.WALK_SPEED,
                    Ability.NO_CLIP,
                    Ability.OPERATOR_COMMANDS
                )
            )
            walkSpeed = 0.1f
            flySpeed = 0.15f
        })
    }

    private val disableFlyNoClipPacket = UpdateAbilitiesPacket().apply {
        playerPermission = PlayerPermission.OPERATOR
        commandPermission = CommandPermission.OWNER
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
                    Ability.OPERATOR_COMMANDS
                )
            )
            walkSpeed = 0.1f
        })
    }

    private var isFlyNoClipEnabled = false

    @OptIn(DelicateCoroutinesApi::class)
    override fun onEnabled() {
        super.onEnabled()
        if (isSessionCreated) {
            // Store original position immediately when enabled
            originalPosition = Vector3f.from(
                session.localPlayer.posX,
                session.localPlayer.posY,
                session.localPlayer.posZ
            )

            GlobalScope.launch {
                for (i in 5 downTo 1) {
                    val countdownMessage = "§l§b[Nova] §r§7FreeCam will enable in §e$i §7seconds"
                    sendCountdownMessage(countdownMessage)
                    delay(1000)
                }

                enableFlyNoClipPacket.uniqueEntityId = session.localPlayer.uniqueEntityId
                session.clientBound(enableFlyNoClipPacket)
                isFlyNoClipEnabled = true
            }
        }
    }

    override fun onDisabled() {
        super.onDisabled()
        if (isSessionCreated && originalPosition != null) {
            // Return to original position when disabled
            val motionPacket = SetEntityMotionPacket().apply {
                runtimeEntityId = session.localPlayer.runtimeEntityId
                motion = originalPosition
            }
            session.clientBound(motionPacket)
            originalPosition = null

            disableFlyNoClipPacket.uniqueEntityId = session.localPlayer.uniqueEntityId
            session.clientBound(disableFlyNoClipPacket)
            isFlyNoClipEnabled = false
        }
    }

    private fun sendCountdownMessage(message: String) {
        val textPacket = TextPacket().apply {
            type = TextPacket.Type.RAW
            this.message = message
            xuid = ""
            sourceName = ""
        }

        session.clientBound(textPacket)
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket && isEnabled) {
            // Handle vertical movement
            if (isFlyNoClipEnabled) {
                var verticalMotion = 0f

                // Space for up, Shift for down
                if (packet.inputData.contains(PlayerAuthInputData.JUMPING)) {
                    verticalMotion = flySpeed
                } else if (packet.inputData.contains(PlayerAuthInputData.SNEAKING)) {
                    verticalMotion = -flySpeed
                }

                if (verticalMotion != 0f) {
                    val motionPacket = SetEntityMotionPacket().apply {
                        runtimeEntityId = session.localPlayer.runtimeEntityId
                        motion = Vector3f.from(0f, verticalMotion, 0f)
                    }
                    session.clientBound(motionPacket)
                }
            }

            interceptablePacket.intercept()
        }
    }
}