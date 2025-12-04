package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.entity.Entity
import com.retrivedmods.wclient.game.entity.LocalPlayer
import com.retrivedmods.wclient.game.entity.Player
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import kotlin.math.sqrt
import kotlin.random.Random

class AutoFightModule : Module("AutoFight", ModuleCategory.Combat) {

    private val maxRange by floatValue("Max Range", 5f, 2f..30f)
    private val attackCps by intValue("CPS", 10, 1..20)
    private val attackPackets by intValue("Packets", 1, 1..5)
    private val criticalHits by boolValue("Critical Hits", false)
    private val multiTarget by boolValue("Multi-Target", false)
    private val maxTargets by intValue("Max Targets", 2, 1..5)
    private val strikeDistance by floatValue("Strike Distance", 1.5f, 0.5f..3f)
    private val returnDelay by intValue("Return Delay", 100, 50..500)
    private val humanizeMovement by boolValue("Humanize Movement", true)
    private val randomizeTiming by boolValue("Randomize Timing", true)

    private var originalPosition: Vector3f? = null
    private var lastAttackTime = 0L
    private var attackStartTime = 0L
    private var isReturning = false

    private val attackDelayMs: Long
        get() = (1000L / attackCps) * if (randomizeTiming) Random.nextInt(80, 120) / 100 else 1

    override fun onDisabled() {
        resetState()
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet !is PlayerAuthInputPacket) return

        val now = System.currentTimeMillis()
        val targets = findTargets()

        if (targets.isEmpty()) {
            resetState()
            return
        }

        if (originalPosition == null && now - lastAttackTime >= attackDelayMs) {
            originalPosition = session.localPlayer.vec3Position
        }

        when {
            originalPosition != null && !isReturning && now - lastAttackTime >= attackDelayMs -> {
                val target = targets.first()
                strikeTarget(target)
                attackStartTime = now
            }

            !isReturning && now - attackStartTime >= returnDelay -> {
                returnToOriginalPosition()
                isReturning = true
            }

            isReturning && now - attackStartTime >= returnDelay + 50 -> {
                resetState()
            }
        }
    }

    private fun resetState() {
        originalPosition = null
        isReturning = false
        attackStartTime = 0L
    }

    private fun findTargets(): List<Entity> {
        return session.level.entityMap.values
            .filter { it.runtimeEntityId != session.localPlayer.runtimeEntityId && it.isValidTarget() }
            .filter { it.distance(session.localPlayer) < maxRange }
            .sortedBy { it.distance(session.localPlayer) }
            .take(if (multiTarget) maxTargets else 1)
    }

    private fun Entity.isValidTarget(): Boolean {
        return this is Player && this !is LocalPlayer && session.level.playerMap[this.uuid]?.name?.isNotBlank() == true
    }

    private fun strikeTarget(target: Entity) {
        val player = session.localPlayer
        val dx = target.vec3Position.x - player.vec3Position.x
        val dz = target.vec3Position.z - player.vec3Position.z
        val distance = sqrt(dx * dx + dz * dz)
        val nx = dx / distance
        val nz = dz / distance

        val offsetX = if (humanizeMovement) Random.nextFloat() * 0.1f - 0.05f else 0f
        val offsetZ = if (humanizeMovement) Random.nextFloat() * 0.1f - 0.05f else 0f

        val targetPos = Vector3f.from(
            target.vec3Position.x - nx * strikeDistance + offsetX,
            target.vec3Position.y + if (criticalHits) 0.5f else 0f,
            target.vec3Position.z - nz * strikeDistance + offsetZ
        )

        val motionX = (targetPos.x - player.vec3Position.x) * 0.5f
        val motionY = (targetPos.y - player.vec3Position.y) * 0.5f
        val motionZ = (targetPos.z - player.vec3Position.z) * 0.5f

        session.clientBound(SetEntityMotionPacket().apply {
            runtimeEntityId = player.runtimeEntityId
            motion = Vector3f.from(motionX, motionY, motionZ)
        })

        session.clientBound(MovePlayerPacket().apply {
            runtimeEntityId = player.runtimeEntityId
            position = targetPos
            rotation = player.vec3Rotation
            mode = MovePlayerPacket.Mode.TELEPORT
            tick = player.tickExists
        })

        repeat(attackPackets) {
            player.attack(target)
        }

        lastAttackTime = System.currentTimeMillis()
    }

    private fun returnToOriginalPosition() {
        originalPosition?.let { pos ->
            val player = session.localPlayer
            val motionX = (pos.x - player.vec3Position.x) * 0.5f
            val motionY = (pos.y - player.vec3Position.y) * 0.5f
            val motionZ = (pos.z - player.vec3Position.z) * 0.5f

            session.clientBound(SetEntityMotionPacket().apply {
                runtimeEntityId = player.runtimeEntityId
                motion = Vector3f.from(motionX, motionY, motionZ)
            })

            session.clientBound(MovePlayerPacket().apply {
                runtimeEntityId = player.runtimeEntityId
                position = pos
                rotation = player.vec3Rotation
                mode = MovePlayerPacket.Mode.TELEPORT
                tick = player.tickExists
            })
        }
    }
}