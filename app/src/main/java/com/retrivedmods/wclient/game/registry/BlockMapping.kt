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

    companion object {
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