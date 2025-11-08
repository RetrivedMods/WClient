package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.entity.*
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class WAuraModule : Module("WAura", ModuleCategory.Combat) {

    private var playersOnly by boolValue("players_only", true)
    private var mobsOnly by boolValue("mobs_only", false)

    private var rangeValue by floatValue("range", 50f, 2f..50f)
    private var cpsValue by intValue("cps", 25, 1..50)
    private var boost by intValue("packets", 2, 1..10)

    private var targetMode by intValue("Target Mode", 0, 0..2) // 0=single, 1=switch, 2=multi
    private var switchDelay by intValue("Switch Delay", 100, 20..100)

    private var lastAttackNanoTime = 0L
    private var lastSwitchTime = 0L
    private var switchIndex = 0
    private var currentTarget: Entity? = null

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            val now = System.nanoTime()
            val nowMillis = System.currentTimeMillis()
            val attackDelay = 1_000_000_000L / cpsValue

            if ((now - lastAttackNanoTime) >= attackDelay) {
                val targets = searchForTargets()
                if (targets.isEmpty()) {
                    currentTarget = null
                    return
                }

                val player = session.localPlayer

                when (targetMode) {
                    0 -> { // Single
                        val target = currentTarget ?: targets.first()
                        if (target.distance(player) <= rangeValue) {
                            repeat(boost) { player.attack(target) }
                            currentTarget = target
                            lastAttackNanoTime = now
                        } else {
                            currentTarget = null
                        }
                    }

                    1 -> { // Switch
                        if ((nowMillis - lastSwitchTime) >= switchDelay) {
                            switchIndex = (switchIndex + 1) % targets.size
                            currentTarget = targets[switchIndex]
                            lastSwitchTime = nowMillis
                        }
                        currentTarget?.let { target ->
                            if (target.distance(player) <= rangeValue) {
                                repeat(boost) { player.attack(target) }
                                lastAttackNanoTime = now
                            }
                        }
                    }

                    2 -> { // Multi
                        for (entity in targets) {
                            if (entity.distance(player) <= rangeValue) {
                                repeat(boost) { player.attack(entity) }
                            }
                        }
                        lastAttackNanoTime = now
                    }
                }
            }
        }
    }

    private fun searchForTargets(): List<Entity> {
        return session.level.entityMap.values
            .filter { it.isTarget() && it.distance(session.localPlayer) <= rangeValue }
            .sortedBy { it.distance(session.localPlayer) }
    }

    private fun Entity.isTarget(): Boolean {
        return when (this) {
            is LocalPlayer -> false
            is Player -> if (mobsOnly) false else !this.isBot()
            is EntityUnknown -> {
                if (mobsOnly) this.identifier in MobList.mobTypes
                else !playersOnly
            }
            else -> false
        }
    }

    private fun Player.isBot(): Boolean {
        if (this is LocalPlayer) return false
        val data = session.level.playerMap[this.uuid]
        return data?.name.isNullOrBlank()
    }
}