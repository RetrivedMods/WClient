package com.retrivedmods.wclient.game.registry

open class ItemDefinition(private val runtimeId: Int, private val identifier: String) :
    org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition {

    override fun getRuntimeId() = runtimeId

    override fun getIdentifier() = identifier

    override fun isComponentBased() = false

    override fun toString(): String {
        return identifier
    }
}

class UnknownItemDefinition(runtimeId: Int): ItemDefinition(runtimeId, "minecraft:unknown")