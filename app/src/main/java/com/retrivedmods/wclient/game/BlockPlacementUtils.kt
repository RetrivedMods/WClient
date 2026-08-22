package com.retrivedmods.wclient.game

import org.cloudburstmc.math.vector.Vector3i
import org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryActionData
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventorySource

/**
 * Shared helpers for modules that place blocks via InventoryTransactionPacket (PistonCrystalModule,
 * SurroundModule). Originally ported by comparing against ProtoHax's EntityLocalPlayer.placeBlock(),
 * but a real placement packet captured in-game (via PacketLoggerModule, comparing a manual placement
 * against what these modules were actually sending) turned up two further problems beyond what was
 * fixed here initially:
 *  - InventoryTransactionPacket.blockDefinition describes the EXISTING block being clicked (used by
 *    the server to sanity-check the client's view of the world) - NOT the new block being placed.
 *    Setting it to the placed block's own definition (this file's old blockDefinitionFor() misuse)
 *    made the server see a mismatch against its own world state and silently reject the whole
 *    transaction.
 *  - inventoriesServerAuthoritative servers require an InventoryActionData entry describing the
 *    consumed item alongside the ITEM_USE transaction; a real client always sends one. Without it
 *    the transaction gets silently dropped on any server using that (the modern default) mode.
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
     * The block InventoryTransactionPacket.blockDefinition actually needs: the EXISTING block at
     * [referencePos] (the one being clicked) - see the class doc above. Use this, not
     * [blockDefinitionFor], when building the packet.
     */
    fun referenceBlockDefinition(session: GameSession, referencePos: Vector3i): BlockDefinition {
        return session.level.getBlockAt(referencePos)
    }

    /**
     * Runtime block definition for [identifier] (e.g. "minecraft:piston") - the block *being
     * placed*. Only useful for [predictLocalBlockChange]; do NOT use this for
     * InventoryTransactionPacket.blockDefinition (see [referenceBlockDefinition] for that). Returns
     * null for non-block items (like "minecraft:end_crystal") that aren't in the block mapping.
     */
    fun blockDefinitionFor(session: GameSession, identifier: String): BlockDefinition? {
        if (!session.isBlockMappingInitialized) return null
        val runtimeId = session.blockMapping.getRuntimeIdByIdentifier(identifier) ?: return null
        return session.blockMapping.getDefinition(runtimeId)
    }

    /**
     * The InventoryActionData a real client always includes alongside an ITEM_USE placement
     * transaction, describing the held item being consumed from the hotbar slot. Missing this is
     * what made every placement from these modules get silently dropped on
     * inventoriesServerAuthoritative servers (the modern default) - confirmed by comparing against
     * a real captured placement packet, which always had exactly one of these.
     */
    fun consumeItemAction(hotbarSlot: Int, current: ItemData): InventoryActionData {
        val afterUse = if (current.count > 1) {
            current.toBuilder().count(current.count - 1).build()
        } else {
            ItemData.AIR
        }
        return InventoryActionData(
            InventorySource.fromContainerWindowId(0),
            hotbarSlot,
            current,
            afterUse
        )
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
