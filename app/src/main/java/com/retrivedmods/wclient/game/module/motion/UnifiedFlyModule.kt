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
import org.cloudburstmc.protocol.bedrock.packet.MobEffectPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.RequestAbilityPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import org.cloudburstmc.protocol.bedrock.packet.UpdateAbilitiesPacket
import com.retrivedmods.wclient.game.data.Effect
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class UnifiedFlyModule : Module("UnifiedFly", ModuleCategory.Motion) {

    // Mode selection
    private val mode by enumValue("Mode", FlyMode.YPORT, FlyMode::class.java)
    
    // General settings
    private val speed by floatValue("Speed", 1.5f, 0.1f..10.0f)
    private val verticalSpeed by floatValue("Vertical Speed", 1.0f, 0.1f..5.0f)
    private val glideSpeed by floatValue("Glide Speed", 0.1f, -0.5f..1.0f)
    
    // Advanced settings
    private val motionInterval by floatValue("Motion Interval", 50.0f, 10.0f..500.0f)
    private val jitterBypass by boolValue("Jitter Bypass", true)
    private val pressJump by boolValue("Press Jump", false)
    
    // YPort specific settings
    private val yPortUpMotion by floatValue("YPort Up Motion", 0.42f, 0.1f..2.0f)
    private val yPortDownMotion by floatValue("YPort Down Motion", -0.42f, -2.0f..-0.1f)
    private val yPortDelay by floatValue("YPort Delay", 0.0f, 0.0f..100.0f)
    
    // Jetpack specific settings
    private val jetpackSpeed by floatValue("Jetpack Speed", 2.5f, 0.5f..10.0f)
    
    // Glide specific settings
    private val glideDescentRate by floatValue("Glide Descent Rate", 0.05f, 0.01f..0.2f)
    
    // Enhanced Motion settings
    private val enhancedJitter by floatValue("Enhanced Jitter", 0.05f, 0.01f..0.2f)
    
    // Internal state
    private var lastMotionTime = 0L
    private var lastYPortTime = 0L
    private var jitterState = false
    private var canFly = false
    private var launchY = 0f
    private var yPortFlag = true

    // Ability packets
    private val flyAbilitiesPacket = UpdateAbilitiesPacket().apply {
        playerPermission = PlayerPermission.OPERATOR
        commandPermission = CommandPermission.OWNER
        abilityLayers.add(AbilityLayer().apply {
            layerType = AbilityLayer.Type.BASE
            abilitiesSet.addAll(Ability.entries.toTypedArray())
            abilityValues.addAll(arrayOf(
                Ability.BUILD,
                Ability.MINE,
                Ability.DOORS_AND_SWITCHES,
                Ability.OPEN_CONTAINERS,
                Ability.ATTACK_PLAYERS,
                Ability.ATTACK_MOBS,
                Ability.MAY_FLY,
                Ability.FLY_SPEED,
                Ability.WALK_SPEED,
                Ability.OPERATOR_COMMANDS
            ))
            walkSpeed = 0.1f
            flySpeed = 0.15f
        })
    }

    private val resetAbilitiesPacket = UpdateAbilitiesPacket().apply {
        playerPermission = PlayerPermission.OPERATOR
        commandPermission = CommandPermission.OWNER
        abilityLayers.add(AbilityLayer().apply {
            layerType = AbilityLayer.Type.BASE
            abilitiesSet.addAll(Ability.entries.toTypedArray())
            abilityValues.addAll(arrayOf(
                Ability.BUILD,
                Ability.MINE,
                Ability.DOORS_AND_SWITCHES,
                Ability.OPEN_CONTAINERS,
                Ability.ATTACK_PLAYERS,
                Ability.ATTACK_MOBS,
                Ability.OPERATOR_COMMANDS
            ))
            walkSpeed = 0.1f
            flySpeed = 0f
        })
    }

    override fun onEnabled() {
        super.onEnabled()
        launchY = session.localPlayer.posY
        yPortFlag = true
        lastMotionTime = 0L
        lastYPortTime = 0L
        jitterState = false
        canFly = false
    }

    override fun onDisabled() {
        super.onDisabled()
        if (isSessionCreated) {
            handleFlyAbilities(false)

            if (mode == FlyMode.GLIDE) {
                try {
                    val removeEffectPacket = MobEffectPacket().apply {
                        event = MobEffectPacket.Event.REMOVE
                        runtimeEntityId = session.localPlayer.runtimeEntityId
                        effectId = Effect.SLOW_FALLING
                    }
                    session.clientBound(removeEffectPacket)
                } catch (e: Exception) {
                    println("Error removing slow falling effect: ${e.message}")
                }
            }
        }
    }

    private fun handleFlyAbilities(enabled: Boolean) {
        if (canFly != enabled) {
            flyAbilitiesPacket.uniqueEntityId = session.localPlayer.uniqueEntityId
            resetAbilitiesPacket.uniqueEntityId = session.localPlayer.uniqueEntityId
            
            if (enabled) {
                session.clientBound(flyAbilitiesPacket)
            } else {
                session.clientBound(resetAbilitiesPacket)
            }
            canFly = enabled
        }
    }

    private val canMove: Boolean
        get() = !pressJump || session.localPlayer.isSprinting

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled || !isSessionCreated) return
        
        val packet = interceptablePacket.packet

        if (mode == FlyMode.VANILLA) {
            if (packet is RequestAbilityPacket && packet.ability == Ability.FLYING) {
                interceptablePacket.intercept()
                return
            }
            if (packet is UpdateAbilitiesPacket) {
                interceptablePacket.intercept()
                return
            }
        }

        if (packet is PlayerAuthInputPacket) {
            try {
                when (mode) {
                    FlyMode.VANILLA -> handleVanillaFly(packet)
                    FlyMode.MOTION -> handleMotionFly(packet)
                    FlyMode.ENHANCED_MOTION -> handleEnhancedMotionFly(packet)
                    FlyMode.JETPACK -> handleJetpackFly(packet)
                    FlyMode.GLIDE -> handleGlideFly(packet)
                    FlyMode.YPORT -> handleYPortFly(packet)
                    FlyMode.TELEPORT -> handleTeleportFly(packet)
                }
            } catch (e: Exception) {
                println("UnifiedFly error: ${e.message}")
            }
        }
    }

    private fun handleVanillaFly(packet: PlayerAuthInputPacket) {
        handleFlyAbilities(true)
        
        var verticalMotion = 0f
        if (packet.inputData.contains(PlayerAuthInputData.JUMPING)) {
            verticalMotion = speed
        } else if (packet.inputData.contains(PlayerAuthInputData.SNEAKING)) {
            verticalMotion = -speed
        }

        if (verticalMotion != 0f) {
            val motionPacket = SetEntityMotionPacket().apply {
                runtimeEntityId = session.localPlayer.runtimeEntityId
                motion = Vector3f.from(0f, verticalMotion, 0f)
            }
            session.clientBound(motionPacket)
        }
    }

    private fun handleMotionFly(packet: PlayerAuthInputPacket) {
        if (System.currentTimeMillis() - lastMotionTime < motionInterval) return
        
        var motionX = 0f
        var motionY = 0f
        var motionZ = 0f

        if (packet.inputData.contains(PlayerAuthInputData.WANT_UP)) {
            motionY = verticalSpeed
        } else if (packet.inputData.contains(PlayerAuthInputData.WANT_DOWN)) {
            motionY = -verticalSpeed
        }

        val inputX = packet.motion.x
        val inputZ = packet.motion.y
        if (inputX != 0f || inputZ != 0f) {
            val yaw = Math.toRadians(packet.rotation.y.toDouble())
            motionX = (inputX * speed * cos(yaw) - inputZ * speed * sin(yaw)).toFloat()
            motionZ = (inputZ * speed * cos(yaw) + inputX * speed * sin(yaw)).toFloat()
        }

        val motionPacket = SetEntityMotionPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            motion = Vector3f.from(motionX, motionY, motionZ)
        }
        session.clientBound(motionPacket)
        lastMotionTime = System.currentTimeMillis()
    }

    private fun handleEnhancedMotionFly(packet: PlayerAuthInputPacket) {
        if (System.currentTimeMillis() - lastMotionTime < motionInterval) return
        
        handleFlyAbilities(true)
        
        val vertical = when {
            packet.inputData.contains(PlayerAuthInputData.WANT_UP) -> verticalSpeed
            packet.inputData.contains(PlayerAuthInputData.WANT_DOWN) -> -verticalSpeed
            else -> glideSpeed
        }

        val inputX = packet.motion.x
        val inputZ = packet.motion.y
        val yaw = Math.toRadians(packet.rotation.y.toDouble()).toFloat()
        val sinYaw = sin(yaw)
        val cosYaw = cos(yaw)

        val strafe = inputX * speed
        val forward = inputZ * speed

        val motionX = strafe * cosYaw - forward * sinYaw
        val motionZ = forward * cosYaw + strafe * sinYaw

        val jitterOffset = if (jitterBypass) {
            if (jitterState) enhancedJitter else -enhancedJitter
        } else 0f

        val motionPacket = SetEntityMotionPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            motion = Vector3f.from(motionX, vertical + jitterOffset, motionZ)
        }

        session.clientBound(motionPacket)
        jitterState = !jitterState
        lastMotionTime = System.currentTimeMillis()
    }

    private fun handleJetpackFly(packet: PlayerAuthInputPacket) {
        if (!canMove) return
        
        val yaw = Math.toRadians(packet.rotation.y.toDouble())
        val pitch = Math.toRadians(packet.rotation.x.toDouble())

        val motionX = -sin(yaw) * cos(pitch) * jetpackSpeed
        val motionY = -sin(pitch) * jetpackSpeed
        val motionZ = cos(yaw) * cos(pitch) * jetpackSpeed

        val motionPacket = SetEntityMotionPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            motion = Vector3f.from(motionX.toFloat(), motionY.toFloat(), motionZ.toFloat())
        }
        session.clientBound(motionPacket)
    }

    private fun handleGlideFly(packet: PlayerAuthInputPacket) {
        if (session.localPlayer.tickExists % 20 == 0L) {
            val slowFallPacket = MobEffectPacket().apply {
                runtimeEntityId = session.localPlayer.runtimeEntityId
                event = MobEffectPacket.Event.ADD
                effectId = Effect.SLOW_FALLING
                amplifier = 0
                isParticles = false
                duration = 360000
            }
            session.clientBound(slowFallPacket)
        }

        val yaw = Math.toRadians(packet.rotation.y.toDouble())
        val pitch = Math.toRadians(packet.rotation.x.toDouble())

        val motionX = -sin(yaw) * cos(pitch) * glideSpeed
        val motionY = -glideDescentRate
        val motionZ = cos(yaw) * cos(pitch) * glideSpeed

        val motionPacket = SetEntityMotionPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            motion = Vector3f.from(motionX.toFloat(), motionY, motionZ.toFloat())
        }
        session.clientBound(motionPacket)
    }

    private fun handleYPortFly(packet: PlayerAuthInputPacket) {
        if (!canMove) return

        if (yPortDelay > 0 && System.currentTimeMillis() - lastYPortTime < yPortDelay) return
        
        val angle = Math.toRadians(session.localPlayer.rotationYaw.toDouble()).toFloat()

        val inputX = packet.motion.x
        val inputZ = packet.motion.y
        val horizontalMotionX = if (inputX != 0f || inputZ != 0f) {
            -sin(angle) * speed
        } else 0f
        val horizontalMotionZ = if (inputX != 0f || inputZ != 0f) {
            cos(angle) * speed
        } else 0f
        
        val motionPacket = SetEntityMotionPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            motion = Vector3f.from(
                horizontalMotionX,
                if (yPortFlag) yPortUpMotion else yPortDownMotion,
                horizontalMotionZ
            )
        }
        session.clientBound(motionPacket)
        yPortFlag = !yPortFlag
        lastYPortTime = System.currentTimeMillis()
    }

    private fun handleTeleportFly(packet: PlayerAuthInputPacket) {
        if (packet.inputData.contains(PlayerAuthInputData.WANT_UP)) {
            launchY += 0.3f
        } else if (packet.inputData.contains(PlayerAuthInputData.WANT_DOWN)) {
            launchY -= 0.3f
        }

        val inputX = packet.motion.x
        val inputZ = packet.motion.y

        if (inputX != 0f || inputZ != 0f) {
            val calcYaw = session.localPlayer.rotationYaw * (PI / 180)
            val c = cos(calcYaw)
            val s = sin(calcYaw)
            
            val finalX = (inputZ * c - inputX * s) * speed
            val finalZ = (inputZ * s + inputX * c) * speed
            
            val motionPacket = SetEntityMotionPacket().apply {
                runtimeEntityId = session.localPlayer.runtimeEntityId
                motion = Vector3f.from(finalX.toFloat(), 0f, finalZ.toFloat())
            }
            session.clientBound(motionPacket)
        }
    }



    enum class FlyMode {
        VANILLA,
        MOTION,
        ENHANCED_MOTION,
        JETPACK,
        GLIDE,
        YPORT,
        TELEPORT
    }
}