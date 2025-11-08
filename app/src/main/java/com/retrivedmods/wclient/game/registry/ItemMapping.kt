package com.retrivedmods.wclient.game.registry

import android.content.Context
import com.google.gson.JsonParser
import org.cloudburstmc.protocol.common.DefinitionRegistry
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition

class ItemMapping(private val runtimeToItem: Map<Int, ItemDefinition>) : DefinitionRegistry<ItemDefinition> {

    override fun getDefinition(runtimeId: Int): ItemDefinition {
        return runtimeToItem[runtimeId] ?: UnknownItemDefinition(runtimeId)
    }

    override fun isRegistered(definition: ItemDefinition): Boolean {
        return definition is UnknownItemDefinition || getDefinition(definition.runtimeId) == definition
    }

    companion object {
        fun read(context: Context, version: Short): ItemMapping {
            val path = "mcpedata/items/runtime_item_states_$version.json"
            context.assets.open(path).use { stream ->
                val reader = stream.reader(Charsets.UTF_8)
                val jsonArray = JsonParser.parseReader(reader).asJsonArray

                val map = mutableMapOf<Int, ItemDefinition>()

                for (element in jsonArray) {
                    val obj = element.asJsonObject
                    val id = obj.get("id")?.asInt ?: continue
                    val name = obj.get("name")?.asString ?: "unknown"

                    val definition = ItemDefinition(id, name)
                    map[id] = definition
                }

                return ItemMapping(map)
            }
        }
    }

}