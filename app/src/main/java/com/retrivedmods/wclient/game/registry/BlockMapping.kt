package com.retrivedmods.wclient.game.registry

import android.content.Context
import org.cloudburstmc.nbt.NBTInputStream
import org.cloudburstmc.nbt.NbtList
import org.cloudburstmc.nbt.NbtMap
import org.cloudburstmc.protocol.common.DefinitionRegistry
import java.io.DataInputStream
import java.util.zip.GZIPInputStream

class BlockMapping(
    private val runtimeToGameMap: Map<Int, BlockDefinition>
) : DefinitionRegistry<org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition> {

    private val gameToRuntimeMap = mutableMapOf<BlockDefinition, Int>()

    init {
        runtimeToGameMap.forEach { (k, v) -> gameToRuntimeMap[v] = k }
    }

    override fun getDefinition(runtimeId: Int): BlockDefinition {
        return runtimeToGameMap[runtimeId] ?: UnknownBlockDefinition(runtimeId)
    }

    override fun isRegistered(definition: org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition): Boolean {
        return definition is UnknownBlockDefinition || getDefinition(definition.runtimeId) == definition
    }

    /**
     * Returns the first runtime id whose identifier matches [identifier] (e.g. "minecraft:obsidian").
     * Block states with extra properties share the same base identifier, so this returns
     * the default/first state found, which is fine for simple full blocks like obsidian.
     */
    fun getRuntimeIdByIdentifier(identifier: String): Int? {
        return runtimeToGameMap.entries.firstOrNull { it.value.identifier == identifier }?.key
    }

    /**
     * Runtime id of "minecraft:air", used by chunk/block storage as the default/empty block.
     * Falls back to 0 if not found (shouldn't happen with a valid mapping).
     */
    val airId: Int by lazy { getRuntimeIdByIdentifier("minecraft:air") ?: 0 }

    companion object {
        /**
         * Builds a BlockMapping straight from the server's own StartGamePacket block palette,
         * instead of a bundled per-version asset file. Each entry's position in [palette] is its
         * runtime id (the modern Bedrock convention, since explicit per-entry runtime ids were
         * removed from the palette format) - unless the entry itself still carries an explicit
         * "runtimeId" tag (older-style palette), which takes priority when present.
         */
        fun fromPalette(palette: List<NbtMap>): BlockMapping {
            val runtimeToBlock = mutableMapOf<Int, BlockDefinition>()
            palette.forEachIndexed { index, entry ->
                val name = entry.getString("name", null) ?: return@forEachIndexed
                val explicitRuntimeId = entry.getInt("runtimeId", Int.MIN_VALUE)
                val runtime = if (explicitRuntimeId != Int.MIN_VALUE) explicitRuntimeId else index
                runtimeToBlock[runtime] = BlockDefinition(runtime, name)
            }
            return BlockMapping(runtimeToBlock)
        }

        fun read(context: Context, version: Short): BlockMapping {
            val path = "mcpedata/blocks/runtime_block_states_$version.dat"
            context.assets.open(path).use { stream ->
                val gzipStream = GZIPInputStream(stream)
                val nbtInput = NBTInputStream(DataInputStream(gzipStream))

                @Suppress("unchecked_cast")
                val tag = nbtInput.readTag() as NbtList<NbtMap>
                val runtimeToBlock = mutableMapOf<Int, BlockDefinition>()

                tag.forEach { subtag ->
                    val runtime = subtag.getInt("runtimeId")
                    val name = subtag.getString("name")
                    runtimeToBlock[runtime] = BlockDefinition(runtime, name)
                }

                return BlockMapping(runtimeToBlock)
            }
        }
    }

}