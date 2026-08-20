package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.util.setPacketField

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
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerHotbarPacket
import kotlin.math.floor

/**
 * Places an obsidian base + end crystal next to a target, plus an (best-effort) piston + redstone
 * setup to try to pop the crystal via a block push instead of hitting it. Ported from the
 * PistonCrystal/PistonCrystal2 C++ reference + ideas from ModuleCrystalAura.kt (ProtoHax), now that
 * WClient has real block lookups via [com.retrivedmods.wclient.game.world.Level.getBlockAt].
 *
 * Known limitations vs the original:
 * - Piston orientation in Bedrock is set from the placer's look direction at placement time. This
 *   fakes that by mutating the outgoing PlayerAuthInputPacket's rotation right before placing, the
 *   same trick AntiCrystal/Surround use for position - it's not guaranteed as reliable as an actual
 *   client rotating for real.
 * - Block data for chunks loaded via blob caching or the newer per-subchunk-request system isn't
 *   tracked (see Level.kt), so getBlockAt() may report air for those even if something's really
 *   there. In that case placements will just be rejected server-side, same as if this weren't
 *   checked at all.
 * - Damage estimation (session.level.simulateExplosionDamage) is a distance-only approximation with
 *   no block-occlusion/exposure factor, so it's an upper bound, not exact.
 * - Direct-attack fallback (autoAttackCrystal) is what actually guarantees detonation; the
 *   piston/redstone path is a best-effort bonus on top of that, not a replacement for it.
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
        const val CRYSTAL_ITEM = "minecraft:end_crystal"
        const val CRYSTAL_ENTITY = "minecraft:ender_crystal"
        const val PISTON = "minecraft:piston"
        const val REDSTONE_BLOCK = "minecraft:redstone_block"
        const val EXPLOSION_SIZE = 6f
    }

    private enum class Step { OBSIDIAN, CRYSTAL, PISTON, REDSTONE, DONE }

    private var step = Step.DONE
    private var obsidianPos: Vector3i? = null
    private var pistonPos: Vector3i? = null
    private var redstonePos: Vector3i? = null
    private var pushDir: Vector3i? = null
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
        obsidianPos = null
        pistonPos = null
        redstonePos = null
        pushDir = null
        tickCounter = 0
        oldSlot = -1
        lastCrystalAttack.clear()
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled || !isSessionCreated) return
        val packet = interceptablePacket.packet
        if (packet !is PlayerAuthInputPacket) return

        // Always try to guarantee detonation of any crystal that's already out there, regardless
        // of whether we're mid-placement - this is the part that actually reliably deals damage.
        if (autoAttackCrystal) {
            attackNearbyCrystals()
        }

        if (step == Step.DONE) {
            val target = findTarget() ?: return
            val base = findPlacementSpot(target) ?: return
            beginPlacement(base, target)
        }

        if (tickCounter < placeDelayTicks) {
            tickCounter++
            return
        }
        tickCounter = 0

        advanceStateMachine(packet)
    }

    // --- target / spot selection -----------------------------------------------------------

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

    /**
     * Looks for a block directly adjacent to the target (one of the 4 cardinal neighbours at the
     * target's feet level) that's solid, with air immediately above it (room for obsidian -> crystal).
     * Real getBlockAt() checks now that Level tracks chunk data.
     */
    private fun findPlacementSpot(target: Entity): Vector3i? {
        val targetPos = target.vec3Position
        val baseX = floor(targetPos.x).toInt()
        val baseY = floor(targetPos.y).toInt()
        val baseZ = floor(targetPos.z).toInt()

        val offsets = listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)
        for ((dx, dz) in offsets) {
            val candidate = Vector3i.from(baseX + dx, baseY - 1, baseZ + dz)
            val at = session.level.getBlockAt(candidate.x, candidate.y + 1, candidate.z)
            // crystal spot itself must be clear; the block below may be air too - in that case
            // we'll place our own obsidian there first (see isObsidianNeeded-equivalent check
            // in beginPlacement).
            if (at.identifier.isAirLike()) {
                if (session.localPlayer.distance(Vector3f.from(candidate.x + 0.5f, candidate.y + 1f, candidate.z + 0.5f)) <= range + 1f) {
                    return candidate
                }
            }
        }
        return null
    }

    private fun String.isAirLike(): Boolean {
        return this == "minecraft:air"
    }

    // --- placement sequencing --------------------------------------------------------------

    private fun beginPlacement(base: Vector3i, target: Entity) {
        val localPlayer = session.localPlayer

        val crystalCenter = Vector3f.from(base.x + 0.5f, base.y + 2f, base.z + 0.5f)
        var estimatedTargetDamage = 0f
        var estimatedSelfDamage = 0f
        session.level.simulateExplosionDamage(
            crystalCenter,
            EXPLOSION_SIZE,
            extraEntities = listOf(localPlayer)
        ) { entity, damage ->
            if (entity.runtimeEntityId == target.runtimeEntityId) estimatedTargetDamage = damage
            if (entity is LocalPlayer) estimatedSelfDamage = damage
        }

        if (estimatedTargetDamage < targetDamageMin) return
        if (estimatedSelfDamage > selfDamageLimit) return

        obsidianPos = base

        // matches PistonCrystal2's isObsidianNeeded(): only place obsidian if the spot doesn't
        // already have a solid block there (e.g. the target is already standing on real ground).
        val obsidianAlreadyPresent = !session.level.getBlockAt(base).identifier.isAirLike()

        // push direction: away from the target, so a piston push (if it lands) knocks the
        // obsidian - and whatever's resting on it - out from under the crystal.
        val targetPos = target.vec3Position
        val dx = base.x + 0.5f - targetPos.x
        val dz = base.z + 0.5f - targetPos.z
        pushDir = if (kotlin.math.abs(dx) > kotlin.math.abs(dz)) {
            Vector3i.from(if (dx > 0) 1 else -1, 0, 0)
        } else {
            Vector3i.from(0, 0, if (dz > 0) 1 else -1)
        }
        pistonPos = base.add(pushDir)
        redstonePos = pistonPos!!.add(pushDir)

        step = if (obsidianAlreadyPresent) Step.CRYSTAL else Step.OBSIDIAN
        tickCounter = 0
    }

    private fun advanceStateMachine(packet: PlayerAuthInputPacket) {
        when (step) {
            Step.OBSIDIAN -> {
                val pos = obsidianPos ?: return resetState()
                if (place(OBSIDIAN, pos, packet, faceUp = true)) {
                    step = Step.CRYSTAL
                }
            }

            Step.CRYSTAL -> {
                val pos = obsidianPos ?: return resetState()
                val crystalPos = pos.add(0, 1, 0)
                if (place(CRYSTAL_ITEM, crystalPos, packet, faceUp = true)) {
                    step = if (usePiston) Step.PISTON else Step.DONE
                    if (!usePiston) resetState()
                }
            }

            Step.PISTON -> {
                val pos = pistonPos ?: return resetState()
                val dir = pushDir ?: return resetState()
                if (place(PISTON, pos, packet, faceUp = false, lookTowards = obsidianPos, pushDir = dir)) {
                    step = Step.REDSTONE
                }
            }

            Step.REDSTONE -> {
                val pos = redstonePos ?: return resetState()
                if (place(REDSTONE_BLOCK, pos, packet, faceUp = false)) {
                    resetState()
                }
            }

            Step.DONE -> {}
        }
    }

    /**
     * Sends one block placement attempt. Returns true whether or not the item was found/placed so
     * the state machine keeps moving forward - we can't confirm server-side success without real
     * world feedback, so we don't retry indefinitely.
     */
    private fun place(
        identifier: String,
        pos: Vector3i,
        authInput: PlayerAuthInputPacket,
        faceUp: Boolean,
        lookTowards: Vector3i? = null,
        pushDir: Vector3i? = null
    ): Boolean {
        val localPlayer = session.localPlayer
        val slot = localPlayer.inventory.searchForItemInHotbar {
            it.definition?.identifier == identifier
        } ?: return true // nothing to place with, don't get stuck retrying forever

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

        val transaction = InventoryTransactionPacket().apply {
            transactionType = InventoryTransactionType.ITEM_USE
            actionType = 0 // click block / place
            blockPosition = pos
            blockFace = if (faceUp) 1 else (pushDir?.let { faceForDirection(it) } ?: 1)
            hotbarSlot = slot
            itemInHand = localPlayer.inventory.hand
            playerPosition = localPlayer.vec3Position
            clickPosition = Vector3f.from(0.5f, 0.5f, 0.5f)
        }

        session.serverBound(transaction)
        return true
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
        // matches the existing (working) HotbarSwitcherModule pattern exactly - only clientBound.
        val packet = PlayerHotbarPacket().apply {
            selectedHotbarSlot = slot
            containerId = 0
            setPacketField("selectHotbarSlot", true)
        }
        session.clientBound(packet)
    }

    // --- crystal detonation fallback -------------------------------------------------------

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
