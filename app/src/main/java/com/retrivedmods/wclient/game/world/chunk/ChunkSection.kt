package com.retrivedmods.wclient.game.world.chunk

import com.retrivedmods.wclient.game.registry.BlockMapping
import io.netty.buffer.ByteBuf

/**
 * Ported from ProtoHax (dev.sora.relay.game.world.chunk.ChunkSection), adapted for WClient.
 *
 * NOTE: the legacy (PocketMine-style, version 0) chunk format is intentionally NOT supported here -
 * it needs a full id+meta -> runtime-id legacy mapping table that WClient doesn't have. Every
 * Bedrock server in practice (and every currently supported protocol version) sends the modern
 * (version 1 or 8-10) format, so this should never come up.
 */
class ChunkSection(private val blockMapping: BlockMapping) {

    var storage = BlockStorage(blockMapping.airId)
        private set

    var populated = false
        private set

    fun read(buf: ByteBuf) {
        populated = true

        val version = buf.readByte().toInt()
        if (version == 1 || version in 8..10) {
            readModern(buf, version)
        } else {
            throw UnsupportedOperationException("chunk section version not supported: $version")
        }
    }

    private fun readModern(buf: ByteBuf, version: Int) {
        val layers = if (version == 1) 1 else buf.readByte().toInt()
        if (version >= 9) {
            buf.readByte() // Y-Index
        }
        if (layers == 0) return
        storage = BlockStorage(buf, true)

        // consume any additional layers (e.g. waterlogging) that we don't track
        repeat(layers - 1) {
            BlockStorage(buf, true)
        }
    }

    fun getBlockAt(x: Int, y: Int, z: Int): Int {
        require(x in 0..15 && y in 0..15 && z in 0..15) { "query out of range (x=$x, y=$y, z=$z)" }
        return storage.getBlock(x, y, z)
    }

    fun setBlockAt(x: Int, y: Int, z: Int, runtimeId: Int) {
        require(x in 0..15 && y in 0..15 && z in 0..15) { "query out of range (x=$x, y=$y, z=$z)" }
        storage.setBlock(x, y, z, runtimeId)
    }
}
