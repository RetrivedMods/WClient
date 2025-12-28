package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.entity.*
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlin.math.cos
import kotlin.math.sin

class KillauraModule : Module("killaura", ModuleCategory.Combat) {



    private var playersOnly by boolValue("players_only", true)
    private var mobsOnly by boolValue("mobs_only", false)

    private var tpAuraEnabled by boolValue("tp_aura", true)
    private var teleportBehind by boolValue("tp_behind", true)
    private var criticalHits by boolValue("critical_hit", true)
    private var strafe by boolValue("strafe", false)

    private var rangeValue by floatValue("range", 9.5f, 2f..16f)
    private var cpsValue by intValue("cps", 20, 5..30)

    private var tpSpeed by intValue("tp_speed", 100, 10..500)
    private var tpYOffset by intValue("tp_y_offset", 1, -10..10)
    private var keepDistance by floatValue("keep_distance", 1.2f, 0.5f..5f)

    private val strafeSpeed by floatValue("strafe_speed", 2.5f, 1f..4f)
    private val strafeRadius by floatValue("strafe_radius", 2.5f, 1f..6f)



    private var lastAttackTime = 0L
    private var tpCooldown = 0L
    private var strafeAngle = 0f


    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
        val packet = interceptablePacket.packet
        if (packet !is PlayerAuthInputPacket) return

        val now = System.currentTimeMillis()
        val delay = 1000L / cpsValue

        if (now - lastAttackTime < delay) return

        val targets = searchForTargets()
        if (targets.isEmpty()) return

        for (target in targets) {

            if (tpAuraEnabled && now - tpCooldown >= tpSpeed) {
                teleportTo(target)
                tpCooldown = now
            }

            if (criticalHits) triggerCriticalHit()

            session.localPlayer.attack(target)

            if (strafe) strafeAroundTarget(target)

            lastAttackTime = now
        }
    }

    /* ===================== CORE LOGIC ===================== */

    private fun searchForTargets(): List<Entity> {
        val player = session.localPlayer
        return session.level.entityMap.values
            .filter { it.distance(player) <= rangeValue && it.isTarget() }
            .sortedBy { it.distance(player) }
    }

    private fun teleportTo(entity: Entity) {
        val player = session.localPlayer
        val pos = entity.vec3Position

        val yawRad = Math.toRadians(entity.vec3Rotation.y.toDouble()).toFloat()
        val behind = Vector3f.from(sin(yawRad), 0f, -cos(yawRad)).normalize()

        val tpPos = if (teleportBehind) {
            Vector3f.from(
                pos.x + behind.x * keepDistance,
                pos.y + tpYOffset,
                pos.z + behind.z * keepDistance
            )
        } else {
            val dir = pos.sub(player.vec3Position).normalize()
            Vector3f.from(
                pos.x - dir.x * keepDistance,
                pos.y + tpYOffset,
                pos.z - dir.z * keepDistance
            )
        }

        session.clientBound(
            MovePlayerPacket().apply {
                runtimeEntityId = player.runtimeEntityId
                position = tpPos
                rotation = entity.vec3Rotation
                mode = MovePlayerPacket.Mode.NORMAL
                onGround = false
                tick = player.tickExists
            }
        )
    }

    private fun strafeAroundTarget(entity: Entity) {
        val pos = entity.vec3Position
        strafeAngle += strafeSpeed
        if (strafeAngle >= 360f) strafeAngle -= 360f

        val x = strafeRadius * cos(strafeAngle)
        val z = strafeRadius * sin(strafeAngle)

        session.clientBound(
            MovePlayerPacket().apply {
                runtimeEntityId = session.localPlayer.runtimeEntityId
                position = pos.add(x.toFloat(), 0f, z.toFloat())
                rotation = Vector3f.ZERO
                mode = MovePlayerPacket.Mode.NORMAL
                onGround = true
                tick = session.localPlayer.tickExists
            }
        )
    }

    private fun triggerCriticalHit() {
        val player = session.localPlayer
        session.clientBound(
            MovePlayerPacket().apply {
                runtimeEntityId = player.runtimeEntityId
                position = player.vec3Position.add(0f, 0.1f, 0f)
                rotation = player.vec3Rotation
                mode = MovePlayerPacket.Mode.NORMAL
                onGround = false
                tick = player.tickExists
            }
        )
    }


    private fun Entity.isTarget(): Boolean = when (this) {
        is LocalPlayer -> false
        is Player -> playersOnly && !isBot()
        is EntityUnknown -> mobsOnly && isMob()
        else -> false
    }

    private fun Player.isBot(): Boolean {
        if (this is LocalPlayer) return false
        return session.level.playerMap[this.uuid]?.name.isNullOrBlank()
    }

    private fun EntityUnknown.isMob(): Boolean {
        return this.identifier in MobList.mobTypes
    }
}
