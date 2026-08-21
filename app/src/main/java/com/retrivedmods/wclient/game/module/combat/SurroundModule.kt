package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.util.setPacketField

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.math.vector.Vector3i
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerHotbarPacket
import kotlin.math.floor

/**
 * Places blocks (obsidian by default) around the player to reduce end crystal / explosion damage.
 *
 * NOTE: unlike the original C++ (Mod-Menu-style) implementation this was ported from, WClient is a
 * relay/proxy client and does not keep a local copy of world block state (no BlockSource / chunk data).
 * Because of that this version can NOT check whether a target position is actually air, replaceable,
 * or already occupied before placing - it just fires placement packets at the 8 positions around the
 * player each tick and lets the server accept or silently reject them, the same way a real client's
 * packet would be validated server-side. "Dynamic" hitbox expansion is also approximated using a fixed
 * player-sized radius per nearby entity (since Entity here has no AABB/hitbox size), not true AABB
 * collision like the original.
 */
class SurroundModule : Module("surround", ModuleCategory.Combat) {

    private var blockIdentifier by stringValue("block", "minecraft:obsidian", null)

    private var placeDelayTicks by intValue("place_delay", 1, 0..20)
    private var blocksPerTick by intValue("blocks_per_tick", 4, 1..8)
    private var placeBelowFeet by boolValue("place_below", true)
    private var center by boolValue("center", true)
    private var dynamic by boolValue("dynamic", true)
    private var dynamicRadius by floatValue("dynamic_radius", 3f, 1f..6f)

    private var placeQueue: MutableList<Vector3i> = mutableListOf()
    private var tickCounter = 0
    private var oldSlot = -1
    private var hasCentered = false

    override fun onEnabled() {
        super.onEnabled()
        placeQueue = mutableListOf()
        tickCounter = 0
        oldSlot = -1
        hasCentered = false
    }

    override fun onDisabled() {
        super.onDisabled()
        placeQueue.clear()
        if (oldSlot != -1 && isSessionCreated) {
            restoreSlot()
        }
        oldSlot = -1
        hasCentered = false
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled || !isSessionCreated) {
            return
        }

        val packet = interceptablePacket.packet
        if (packet !is PlayerAuthInputPacket) {
            return
        }

        val localPlayer = session.localPlayer

        val obsidianSlot = localPlayer.inventory.searchForItemInHotbar {
            it.definition?.identifier == blockIdentifier
        }

        if (obsidianSlot == null) {
            // nothing to place with, don't spam the server
            return
        }

        if (oldSlot == -1) {
            oldSlot = localPlayer.inventory.heldItemSlot
        }

        if (center && !hasCentered) {
            val pos = localPlayer.vec3Position
            val centeredPos = Vector3f.from(floor(pos.x) + 0.5f, pos.y, floor(pos.z) + 0.5f)
            packet.position = centeredPos
            hasCentered = true
        }

        if (placeQueue.isEmpty()) {
            placeQueue = computePlacements(localPlayer.vec3Position).toMutableList()
        }

        if (tickCounter < placeDelayTicks) {
            tickCounter++
            return
        }
        tickCounter = 0

        if (localPlayer.inventory.heldItemSlot != obsidianSlot) {
            switchToSlot(obsidianSlot)
        }

        var placed = 0
        val iterator = placeQueue.iterator()
        while (iterator.hasNext() && placed < blocksPerTick) {
            val target = iterator.next()
            placeBlock(target)
            iterator.remove()
            placed++
        }
    }

    private fun computePlacements(playerPos: Vector3f): List<Vector3i> {
        val baseX = floor(playerPos.x).toInt()
        val baseY = floor(playerPos.y).toInt() - if (placeBelowFeet) 1 else 0
        val baseZ = floor(playerPos.z).toInt()

        var minX = -1
        var maxX = 1
        var minZ = -1
        var maxZ = 1

        if (dynamic) {
            val level = session.level
            level.entityMap.values.forEach { entity ->
                if (entity.distance(playerPos) > dynamicRadius) return@forEach
                val dx = entity.posX - playerPos.x
                val dz = entity.posZ - playerPos.z
                if (dx > 1.2f) maxX = maxOf(maxX, 2)
                if (dx < -1.2f) minX = minOf(minX, -2)
                if (dz > 1.2f) maxZ = maxOf(maxZ, 2)
                if (dz < -1.2f) minZ = minOf(minZ, -2)
            }
        }

        val positions = mutableListOf<Vector3i>()
        for (x in minX..maxX) {
            for (z in minZ..maxZ) {
                if (x == 0 && z == 0) continue
                // only the ring around the player, skip interior tiles when the box grew beyond 3x3
                if (x !in -1..1 && z !in -1..1) continue
                positions.add(Vector3i.from(baseX + x, baseY, baseZ + z))
            }
        }
        return positions
    }

    private fun switchToSlot(slot: Int) {
        val packet = PlayerHotbarPacket().apply {
            selectedHotbarSlot = slot
            containerId = 0
            setPacketField("selectHotbarSlot", true)
        }
        session.serverBound(packet)
        session.clientBound(packet)
    }

    private fun restoreSlot() {
        switchToSlot(oldSlot)
    }

    private fun placeBlock(pos: Vector3i) {
        val localPlayer = session.localPlayer
        val runtimeId = session.blockMapping.getRuntimeIdByIdentifier(blockIdentifier) ?: return
        val definitionToPlace = session.blockMapping.getDefinition(runtimeId)

        // Find a real, currently non-air neighbor to click so the new block actually lands at
        // `pos` - see Level.findPlacementReference for why. If nothing usable is found (target
        // isn't air, chunk not loaded, or genuinely floating in open air), skip this position
        // rather than fire a packet the server will just drop.
        val (referencePos, face) = session.level.findPlacementReference(pos) ?: return

        localPlayer.placeBlock(pos, referencePos, face, definitionToPlace)
    }
}
