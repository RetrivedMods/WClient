package com.inkclient.modules.resource

/**
 * Simple in-memory repository for available resource packs.
 * In a production client this would enumerate filesystem entries, internal bundled packs,
 * and maybe remote pack indexes.
 */
data class ResourcePack(
    val id: String,        // unique id, e.g., path or UUID
    val displayName: String,
    val path: String,      // absolute or virtual path
    val author: String? = null,
    val isActive: Boolean = false
)

object ResourcePackRepository {
    // A simple mutable list to represent packs available to the client.
    private val packs = mutableListOf<ResourcePack>()

    @Synchronized
    fun list(): List<ResourcePack> = packs.toList()

    @Synchronized
    fun add(pack: ResourcePack) {
        packs.removeAll { it.id == pack.id }
        packs += pack
    }

    @Synchronized
    fun remove(packId: String) {
        packs.removeAll { it.id == packId }
    }

    @Synchronized
    fun update(pack: ResourcePack) {
        packs.replaceAll { if (it.id == pack.id) pack else it }
    }

    @Synchronized
    fun clear() {
        packs.clear()
    }
}
