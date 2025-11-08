package com.retrivedmods.wclient.game.registry

open class BlockDefinition(private val runtimeId: Int, val identifier: String):
    org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition {

    override fun getRuntimeId() = runtimeId

    override fun toString(): String {
        return identifier
    }
}

class UnknownBlockDefinition(runtimeId: Int): BlockDefinition(runtimeId, "minecraft:unknown")