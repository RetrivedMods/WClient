package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.entity.Entity
import com.retrivedmods.wclient.game.entity.EntityUnknown
import com.retrivedmods.wclient.game.entity.LocalPlayer
import com.retrivedmods.wclient.game.entity.Player
import com.retrivedmods.wclient.game.friend.FriendManager
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.math.vector.Vector3i
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerHotbarPacket
import kotlin.math.floor

/**
 * Ported from the PistonCrystal.h/.cpp reference, now backed by real block data
 * (session.level.getBlockAt) rather than placing blind. Follows the original's
 * findValidPlacement/calculatePlacement/isValidPlacement structure:
 *  - try each of the 4 cardinal directions from the target, closest-to-player first
 *  - for each direction, try yLevel 0 then 1 (target's feet level, then one above)
 *  - a placement is valid if the crystal spot is air with air above and an obsidian/bedrock
 *    base below it, and both the piston and redstone spots are placeable
 *
 * Not ported: the "dynamic"/shift perpendicular-offset placement variant, and the
 * target/player AABB-collision checks (WClient's Entity has no hitbox width/height to check
 * against). Piston orientation is approximated the same way Surround/AntiCrystal do it - faking
 * the outgoing PlayerAuthInputPacket's rotation just before placing - which is best-effort, not
 * guaranteed reliable. Direct-attack of any spawned crystal remains the fallback that actually
 * guarantees a detonation regardless of whether the piston geometry landed cleanly.
 */
class PistonCrystalModule : Module("piston_crystal", ModuleCategory.Combat) {

    private var range by floatValue("range", 6f, 2f..10f)
    private var placeDelayTicks by intValue("place_delay", 2, 0..20)
    private var usePiston by boolValue("use_piston", true)
    private var fakeRotation by boolValue("fake_rotation", true)
    private var autoAttackCrystal by boolValue("auto_attack_crystal", true)
    private var selfDamageLimit by floatValue("self_damage_limit", 12f, 0f..36f)
    private var targetDamageMin by floatValue("target_damage_min", 4f, 0f..36f)
    private var playersOnly by boolValue("players_only", true)
    private var antiBot by boolValue("anti_bot", true)

    private companion object {
        const val OBSIDIAN = "minecraft:obsidian"
        const val BEDROCK = "minecraft:bedrock"
        const val CRYSTAL_ITEM = "minecraft:end_crystal"
        const val CRYSTAL_ENTITY = "minecraft:ender_crystal"
        const val PISTON = "minecraft:piston"
        const val REDSTONE_BLOCK = "minecraft:redstone_block"
        const val EXPLOSION_SIZE = 6f

        // Direction.X_PLUS, X_MINUS, Z_PLUS, Z_MINUS from PistonCrystal.h
        val DIRECTIONS = listOf(Vector3i.from(1, 0, 0), Vector3i.from(-1, 0, 0), Vector3i.from(0, 0, 1), Vector3i.from(0, 0, -1))
    }

    private data class Placement(val crystalPos: Vector3i, val pistonPos: Vector3i, val redstonePos: Vector3i, val dir: Vector3i)

    private enum class Step { CRYSTAL, PISTON, REDSTONE, DONE }

    private var step = Step.DONE
    private var placement: Placement? = null
    private var tickCounter = 0
    private var oldSlot = -1
    private val lastCrystalAttack = HashMap<Long, Long>()

    override fun onEnabled() {
        super.onEnabled()
        resetState()
    }

    override fun onDisabled() {
        super.onDisabled()
        if (oldSlot != -1 && isSessionCreated) {
            switchToSlot(oldSlot)
        }
        resetState()
    }

    private fun resetState() {
        step = Step.DONE
        placement = null
        tickCounter = 0
        oldSlot = -1
        lastCrystalAttack.clear()
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled || !isSessionCreated) return
        val packet = interceptablePacket.packet
        if (packet !is PlayerAuthInputPacket) return

        // Guaranteed detonation path - runs every tick regardless of placement state.
        if (autoAttackCrystal) {
            attackNearbyCrystals()
        }

        if (step == Step.DONE) {
            val target = findTarget()
            if (target != null) {
                val found = findValidPlacement(target)
                if (found != null) {
                    placement = found
                    step = Step.CRYSTAL
                    tickCounter = 0
                }
            }
        }

        if (tickCounter < placeDelayTicks) {
            tickCounter++
            return
        }
        tickCounter = 0

        advanceStateMachine(packet)
    }

    // --- target selection -------------------------------------------------------------------

    private fun findTarget(): Entity? {
        val localPlayer = session.localPlayer
        return session.level.entityMap.values
            .filter { it.distance(localPlayer) <= range }
            .filter { it.isValidTarget() }
            .sortedBy { it.distance(localPlayer) }
            .firstOrNull()
    }

    private fun Entity.isValidTarget(): Boolean {
        return when (this) {
            is LocalPlayer -> false
            is Player -> {
                if (!playersOnly) return false
                if (FriendManager.isFriend(this.uuid)) return false
                if (antiBot && session.level.playerMap[this.uuid] == null) return false
                true
            }

            is EntityUnknown -> !playersOnly
            else -> false
        }
    }

    // --- placement search (PistonCrystal.cpp: findValidPlacement/calculatePlacement) --------

    private fun findValidPlacement(target: Entity): Placement? {
        val targetPos = target.vec3Position
        val targetBlockPos = Vector3i.from(floor(targetPos.x).toInt(), floor(targetPos.y).toInt(), floor(targetPos.z).toInt())

        val localPlayer = session.localPlayer
        val orderedDirs = DIRECTIONS.sortedBy { dir ->
            val testPos = Vector3f.from(
                targetBlockPos.x + dir.x * 3f,
                targetBlockPos.y.toFloat(),
                targetBlockPos.z + dir.z * 3f
            )
            localPlayer.distance(testPos)
        }

        for (dir in orderedDirs) {
            for (yLevel in 0..1) {
                val config = calculatePlacement(targetBlockPos, dir, yLevel)
                if (isValidPlacement(config, target)) {
                    return config
                }
            }
        }
        return null
    }

    private fun calculatePlacement(targetPos: Vector3i, dir: Vector3i, yLevel: Int): Placement {
        val crystalPos = targetPos.add(dir.x, yLevel, dir.z)
        val pistonPos = targetPos.add(dir.x * 2, yLevel, dir.z * 2)
        val redstonePos = targetPos.add(dir.x * 3, yLevel, dir.z * 3)
        return Placement(crystalPos, pistonPos, redstonePos, dir)
    }

    private fun isAir(pos: Vector3i) = session.level.getBlockAt(pos).identifier == "minecraft:air"

    private fun isValidCrystalBase(pos: Vector3i): Boolean {
        val id = session.level.getBlockAt(pos).identifier
        return id == OBSIDIAN || id == BEDROCK
    }

    private fun canPlaceCrystal(pos: Vector3i): Boolean {
        return isAir(pos) && isAir(pos.add(0, 1, 0)) && isValidCrystalBase(pos.add(0, -1, 0))
    }

    /** Loosely matches canBeBuiltOver(): only air here (WClient has no full block-property table). */
    private fun canPlaceBlock(pos: Vector3i): Boolean = isAir(pos)

    private fun isValidPlacement(config: Placement, target: Entity): Boolean {
        if (!canPlaceCrystal(config.crystalPos)) return false
        if (!canPlaceBlock(config.pistonPos)) return false
        if (!canPlaceBlock(config.redstonePos)) return false

        val crystalCenter = Vector3f.from(config.crystalPos.x + 0.5f, config.crystalPos.y + 0.5f, config.crystalPos.z + 0.5f)
        if (target.distance(crystalCenter) > range) return false

        var estimatedTargetDamage = 0f
        var estimatedSelfDamage = 0f
        session.level.simulateExplosionDamage(
            Vector3f.from(config.crystalPos.x + 0.5f, config.crystalPos.y + 0.5f, config.crystalPos.z + 0.5f),
            EXPLOSION_SIZE,
            extraEntities = listOf(session.localPlayer)
        ) { entity, damage ->
            if (entity.runtimeEntityId == target.runtimeEntityId) estimatedTargetDamage = damage
            if (entity is LocalPlayer) estimatedSelfDamage = damage
        }
        if (estimatedTargetDamage < targetDamageMin) return false
        if (estimatedSelfDamage > selfDamageLimit) return false

        return true
    }

    // --- placement sequencing ----------------------------------------------------------------

    private fun advanceStateMachine(packet: PlayerAuthInputPacket) {
        val config = placement ?: return resetState()

        when (step) {
            Step.CRYSTAL -> {
                if (place(CRYSTAL_ITEM, config.crystalPos, packet, lookTowards = config.crystalPos)) {
                    step = if (usePiston) Step.PISTON else Step.DONE
                    if (!usePiston) resetState()
                }
            }

            Step.PISTON -> {
                if (place(PISTON, config.pistonPos, packet, lookTowards = config.crystalPos, preferredFace = faceForDirection(Vector3i.from(-config.dir.x, 0, -config.dir.z)))) {
                    step = Step.REDSTONE
                }
            }

            Step.REDSTONE -> {
                if (place(REDSTONE_BLOCK, config.redstonePos, packet, lookTowards = null)) {
                    resetState()
                }
            }

            Step.DONE -> {}
        }
    }

    private fun place(
        identifier: String,
        pos: Vector3i,
        authInput: PlayerAuthInputPacket,
        lookTowards: Vector3i?,
        preferredFace: Int? = null
    ): Boolean {
        val localPlayer = session.localPlayer
        val slot = localPlayer.inventory.searchForItemInHotbar {
            it.definition?.identifier == identifier
        } ?: return true // don't get stuck retrying forever if we're out of the item

        if (oldSlot == -1) {
            oldSlot = localPlayer.inventory.heldItemSlot
        }
        if (localPlayer.inventory.heldItemSlot != slot) {
            switchToSlot(slot)
        }

        if (fakeRotation && lookTowards != null) {
            val eye = localPlayer.vec3Position
            val dx = (lookTowards.x + 0.5f) - eye.x
            val dz = (lookTowards.z + 0.5f) - eye.z
            val yaw = Math.toDegrees(kotlin.math.atan2(-dx.toDouble(), dz.toDouble())).toFloat()
            authInput.rotation = Vector3f.from(0f, yaw, yaw)
        }

        // Real Bedrock block placement clicks an existing SOLID block's face; the server computes
        // the new block's position as (clickedBlockPos + faceNormal). Sending blockPosition = pos
        // (the empty cell we want to fill) was wrong - resolve a real solid neighbour to click
        // instead, preferring one that gives the orientation we actually want (e.g. for the
        // piston's push direction) when possible.
        val (refPos, face) = resolvePlacementReference(pos, preferredFace) ?: return true

        // Reuse LocalPlayer.placeBlock so piston placement has the same
        // server-authoritative inventory handling as ProtoHax.
        val definition = localPlayer.inventory.hand.blockDefinition ?: return true
        localPlayer.placeBlock(pos, refPos, face, definition)
        return true
    }

    /**
     * Finds a real, currently-solid block adjacent to [target] and the face of it that faces
     * [target], so a placement packet can click that face like a real client would (see place()).
     * If [preferredFace] is given (e.g. to aim the piston in a specific push direction) and that
     * particular neighbour happens to be solid, it's used first; otherwise falls back to any
     * solid neighbour, preferring straight down.
     * Bedrock face indices: 0=down,1=up,2=north(-z),3=south(+z),4=west(-x),5=east(+x).
     */
    private fun resolvePlacementReference(target: Vector3i, preferredFace: Int? = null): Pair<Vector3i, Int>? {
        val candidates = listOf(
            target.add(0, -1, 0) to 1,
            target.add(0, 0, -1) to 3,
            target.add(0, 0, 1) to 2,
            target.add(-1, 0, 0) to 5,
            target.add(1, 0, 0) to 4,
            target.add(0, 1, 0) to 0
        )
        if (preferredFace != null) {
            val preferred = candidates.firstOrNull { it.second == preferredFace }
            if (preferred != null && session.level.getBlockAt(preferred.first).identifier != "minecraft:air") {
                return preferred
            }
        }
        return candidates.firstOrNull { (pos, _) -> session.level.getBlockAt(pos).identifier != "minecraft:air" }
    }

    /** Bedrock block face indices: 0=down,1=up,2=north,3=south,4=west,5=east */
    private fun faceForDirection(dir: Vector3i): Int {
        return when {
            dir.x > 0 -> 5
            dir.x < 0 -> 4
            dir.z > 0 -> 3
            dir.z < 0 -> 2
            else -> 1
        }
    }

    private fun switchToSlot(slot: Int) {
        val packet = PlayerHotbarPacket().apply {
            selectedHotbarSlot = slot
            containerId = 0
            isSelectHotbarSlot = true
        }
        // The hotbar selection has to reach the server as well as our local
        // inventory tracker.
        session.serverBound(packet)
        session.clientBound(packet)
    }

    // --- crystal detonation fallback ----------------------------------------------------------

    private fun attackNearbyCrystals() {
        val localPlayer = session.localPlayer
        val now = System.currentTimeMillis()

        session.level.entityMap.values
            .filterIsInstance<EntityUnknown>()
            .filter { it.identifier == CRYSTAL_ENTITY }
            .filter { it.distance(localPlayer) <= range }
            .forEach { crystal ->
                val last = lastCrystalAttack[crystal.runtimeEntityId] ?: 0L
                if (now - last < 100L) return@forEach
                lastCrystalAttack[crystal.runtimeEntityId] = now
                localPlayer.attack(crystal)
            }
    }
}
