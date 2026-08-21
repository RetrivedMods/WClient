package com.retrivedmods.wclient.game.module.combat

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
 * Ported from the reference Surround.cpp, now using real block data (session.level.getBlockAt)
 * instead of blindly firing placements. Follows the same ring-without-corners shape and
 * dynamic-expansion idea as the original, adapted to what WClient can actually see: entities
 * don't carry a hitbox width/height here, so "dynamic" expansion uses a flat per-entity margin
 * check against the ring cells instead of true AABB intersection.
 */
class SurroundModule : Module("surround", ModuleCategory.Combat) {

    private var placeDelayTicks by intValue("place_delay", 1, 0..20)
    private var blocksPerTick by intValue("blocks_per_tick", 1, 1..10)
    private var airPlace by boolValue("air_place", false)
    private var center by boolValue("center", true)
    private var dynamic by boolValue("dynamic", true)
    private var dynamicMargin by floatValue("dynamic_margin", 0.6f, 0f..2f)
    private var placeButton by boolValue("button", true)
    private var fakeRotation by boolValue("rotate", false)

    private companion object {
        const val OBSIDIAN = "minecraft:obsidian"
        const val BUTTON = "minecraft:stone_button"
    }

    private var placeList: MutableList<Vector3i> = mutableListOf()
    private var tickCounter = 0
    private var oldSlot = -1
    private var hasCentered = false

    override fun onEnabled() {
        super.onEnabled()
        placeList = mutableListOf()
        tickCounter = 0
        oldSlot = -1
        hasCentered = false
    }

    override fun onDisabled() {
        super.onDisabled()
        placeList.clear()
        if (oldSlot != -1 && isSessionCreated) {
            switchToSlot(oldSlot)
        }
        oldSlot = -1
        hasCentered = false
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled || !isSessionCreated) return
        val packet = interceptablePacket.packet
        if (packet !is PlayerAuthInputPacket) return

        val localPlayer = session.localPlayer

        if (center && !hasCentered) {
            val pos = localPlayer.vec3Position
            packet.position = Vector3f.from(floor(pos.x) + 0.5f, pos.y, floor(pos.z) + 0.5f)
            hasCentered = true
        }

        val obsidianSlot = localPlayer.inventory.searchForItemInHotbar {
            it.definition?.identifier == OBSIDIAN
        }

        placeList = computePlaceList()

        if (fakeRotation && placeList.isNotEmpty()) {
            val eye = localPlayer.vec3Position
            val target = placeList.first()
            val dx = (target.x + 0.5f) - eye.x
            val dz = (target.z + 0.5f) - eye.z
            val yaw = Math.toDegrees(kotlin.math.atan2(-dx.toDouble(), dz.toDouble())).toFloat()
            packet.rotation = Vector3f.from(packet.rotation.x, yaw, yaw)
        }

        if (obsidianSlot != null && placeList.isNotEmpty()) {
            if (oldSlot == -1) {
                oldSlot = localPlayer.inventory.heldItemSlot
            }

            if (tickCounter >= placeDelayTicks) {
                tickCounter = 0
                if (localPlayer.inventory.heldItemSlot != obsidianSlot) {
                    switchToSlot(obsidianSlot)
                }

                var placed = 0
                val iterator = placeList.iterator()
                while (iterator.hasNext() && placed < blocksPerTick) {
                    val pos = iterator.next()
                    place(OBSIDIAN, pos, obsidianSlot)
                    placed++
                }
            } else {
                tickCounter++
            }
        }

        if (placeButton) {
            val buttonSlot = localPlayer.inventory.searchForItemInHotbar {
                it.definition?.identifier == BUTTON
            }
            if (buttonSlot != null) {
                val buttonPos = Vector3i.from(
                    floor(localPlayer.vec3Position.x).toInt(),
                    floor(localPlayer.vec3Position.y).toInt() - 1,
                    floor(localPlayer.vec3Position.z).toInt()
                )
                place(BUTTON, buttonPos, buttonSlot)
            }
        }
    }

    /** [Surround.cpp]'s canPlaceBlock(): the target itself must be air (or, with airPlace, anything). */
    private fun canPlaceAt(pos: Vector3i): Boolean {
        if (airPlace) return true
        return session.level.getBlockAt(pos).identifier == "minecraft:air"
    }

    private fun computePlaceList(): MutableList<Vector3i> {
        val localPlayer = session.localPlayer
        val pos = localPlayer.vec3Position
        val currentPos = Vector3i.from(floor(pos.x).toInt(), floor(pos.y + 0.5f).toInt(), floor(pos.z).toInt())

        var xStart = -1
        var zStart = -1
        var xEnd = 1
        var zEnd = 1

        if (dynamic) {
            session.level.entityMap.values.forEach { entity ->
                val d = entity.distance(pos)
                if (d > 4f) return@forEach
                val dx = entity.posX - pos.x
                val dz = entity.posZ - pos.z
                if (dx <= xStart + 1 + dynamicMargin && dx >= xStart - dynamicMargin) xStart -= 1
                if (dz <= zStart + 1 + dynamicMargin && dz >= zStart - dynamicMargin) zStart -= 1
                if (dx >= xEnd - 1 - dynamicMargin && dx <= xEnd + dynamicMargin) xEnd += 1
                if (dz >= zEnd - 1 - dynamicMargin && dz <= zEnd + dynamicMargin) zEnd += 1
            }
        }

        val result = mutableListOf<Vector3i>()
        for (x in xStart..xEnd) {
            for (z in zStart..zEnd) {
                // skip the 4 corners, matching Surround.cpp's ring-without-corners shape
                if ((x == xStart || x == xEnd) && (z == zStart || z == zEnd)) continue

                if (x > xStart && x < xEnd && z > zStart && z < zEnd) {
                    // strictly interior cell (only reachable once dynamic expansion has grown the
                    // box past the base 3x3): floor it in, one level down
                    val placePos = currentPos.add(x, -1, z)
                    if (canPlaceAt(placePos)) result.add(placePos)
                    continue
                }

                val placePos = currentPos.add(x, 0, z)
                val below = currentPos.add(x, -1, z)
                // only bother with the "wall" cell if there's solid ground for it to stand on
                if (session.level.getBlockAt(below).identifier == "minecraft:air" && !airPlace) continue

                if (canPlaceAt(placePos)) {
                    result.add(placePos)
                } else if (canPlaceAt(below)) {
                    result.add(below)
                }
            }
        }

        result.sortBy { it.distanceSq(currentPos) }
        return result
    }

    private fun Vector3i.distanceSq(other: Vector3i): Int {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return dx * dx + dy * dy + dz * dz
    }

    private fun place(identifier: String, pos: Vector3i, slot: Int) {
        val localPlayer = session.localPlayer

        // Real Bedrock block placement clicks an existing SOLID block's face; the server computes
        // the new block's position as (clickedBlockPos + faceNormal) itself. Sending blockPosition
        // = pos (the empty air cell we want to fill) was wrong - there's nothing solid there to
        // click, so the server had nothing valid to place against. Resolve a real solid neighbour
        // to click instead.
        val (refPos, face) = resolvePlacementReference(pos) ?: return

        val packet = InventoryTransactionPacket().apply {
            transactionType = InventoryTransactionType.ITEM_USE
            actionType = 0
            blockPosition = refPos
            blockFace = face
            hotbarSlot = slot
            itemInHand = localPlayer.inventory.hand
            playerPosition = localPlayer.vec3Position
            clickPosition = Vector3f.from(0.5f, 0.5f, 0.5f)
            blockDefinition = session.level.getBlockAt(refPos)
        }
        session.serverBound(packet)
    }

    /**
     * Finds a real, currently-solid block adjacent to [target] and the face of it that faces
     * [target], so a placement packet can click that face like a real client would (see place()).
     * Prefers straight down, since that's the most common/expected case for these two modules.
     * Bedrock face indices: 0=down,1=up,2=north(-z),3=south(+z),4=west(-x),5=east(+x).
     */
    private fun resolvePlacementReference(target: Vector3i): Pair<Vector3i, Int>? {
        val candidates = listOf(
            target.add(0, -1, 0) to 1,
            target.add(0, 0, -1) to 3,
            target.add(0, 0, 1) to 2,
            target.add(-1, 0, 0) to 5,
            target.add(1, 0, 0) to 4,
            target.add(0, 1, 0) to 0
        )
        return candidates.firstOrNull { (pos, _) -> session.level.getBlockAt(pos).identifier != "minecraft:air" }
    }

    private fun switchToSlot(slot: Int) {
        val packet = PlayerHotbarPacket().apply {
            selectedHotbarSlot = slot
            containerId = 0
            isSelectHotbarSlot = true
        }
        session.clientBound(packet)
    }
}
