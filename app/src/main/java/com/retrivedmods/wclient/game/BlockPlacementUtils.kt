package com.retrivedmods.wclient.game

import org.cloudburstmc.math.vector.Vector3i
import org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition

/**
 * Shared helpers for modules that place blocks via InventoryTransactionPacket (PistonCrystalModule,
 * SurroundModule). Ported by comparing against ProtoHax's EntityLocalPlayer.placeBlock()/useItem(),
 * which turned up two things our ported modules were missing - most likely why every placement was
 * being silently ignored/rejected by the server:
 *  - InventoryTransactionPacket.blockDefinition (which block actually gets placed) was never set
 *  - blockPosition/blockFace were pointing at the *empty* target spot itself instead of an existing
 *    solid neighbor block being "clicked" - which is what those two fields actually mean on the
 *    wire: the new block appears on the far side of the clicked face of an *existing* block, you
 *    can't click a position that's air.
 */
object BlockPlacementUtils {

    /** (face normal, WClient block-face index) pairs, tried in this order. 0=down,1=up,2=north,3=south,4=west,5=east. */
    private val FACES = listOf(
        Vector3i.from(0, -1, 0) to 0, // down - tried first: the common "build on top of solid ground" case
        Vector3i.from(0, 1, 0) to 1,  // up
        Vector3i.from(0, 0, -1) to 2, // north
        Vector3i.from(0, 0, 1) to 3,  // south
        Vector3i.from(-1, 0, 0) to 4, // west
        Vector3i.from(1, 0, 0) to 5   // east
    )

    /**
     * Finds an existing non-air neighbor of [pos] to use as the InventoryTransactionPacket's
     * blockPosition/blockFace (the new block ends up placed at [pos], on the far side of the
     * returned face). Returns null if [pos] is fully isolated (no solid neighbor at all) - a real
     * Minecraft client couldn't place there either in that case.
     */
    fun findReferenceBlock(session: GameSession, pos: Vector3i): Pair<Vector3i, Int>? {
        for ((normal, face) in FACES) {
            val neighbor = pos.add(-normal.x, -normal.y, -normal.z)
            if (session.level.getBlockAt(neighbor).identifier != "minecraft:air") {
                return neighbor to face
            }
        }
        return null
    }

    /**
     * Runtime block definition for [identifier] (e.g. "minecraft:piston"), for use as
     * InventoryTransactionPacket.blockDefinition. Returns null for non-block items (like
     * "minecraft:end_crystal") - those items aren't in the block mapping at all, and the packet's
     * blockDefinition is expected to stay unset for them.
     */
    fun blockDefinitionFor(session: GameSession, identifier: String): BlockDefinition? {
        if (!session.isBlockMappingInitialized) return null
        val runtimeId = session.blockMapping.getRuntimeIdByIdentifier(identifier) ?: return null
        return session.blockMapping.getDefinition(runtimeId)
    }

    /**
     * Updates WClient's own tracked world state immediately instead of waiting for the server to
     * echo an UpdateBlockPacket back. Matters for placement sequences (piston -> crystal ->
     * redstone, or Surround's many-blocks-per-tick ring) where a later step's validity checks query
     * session.level.getBlockAt() and would otherwise still see stale (air) data for up to a full
     * round-trip. Mirrors ProtoHax's EntityLocalPlayer.placeBlock(), which does the same local
     * prediction via session.level.setBlockIdAt() before sending the transaction.
     */
    fun predictLocalBlockChange(session: GameSession, pos: Vector3i, definition: BlockDefinition?) {
        if (definition == null) return
        session.level.setBlockIdAt(pos.x, pos.y, pos.z, definition.runtimeId)
    }
}
