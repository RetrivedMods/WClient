package com.retrivedmods.wrelay.client

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import kotlin.random.Random

object ClientIdentification {

    fun generateRealisticClientGUID(): Long {
        val timestamp = System.currentTimeMillis()
        val random = Random.nextLong(0, 0xFFFFFF)
        return (timestamp shl 24) or random
    }

    fun generateInternalAddresses(): List<String> {
        return listOf(
            "192.168.1.${Random.nextInt(2, 254)}",
            "10.0.0.${Random.nextInt(2, 254)}",
            "172.16.0.${Random.nextInt(2, 254)}"
        )
    }

    fun createMinecraftUnconnectedMagic(): ByteBuf {
        val magic = byteArrayOf(
            0x00.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0x00.toByte(),
            0xFE.toByte(), 0xFE.toByte(), 0xFE.toByte(), 0xFE.toByte(),
            0xFD.toByte(), 0xFD.toByte(), 0xFD.toByte(), 0xFD.toByte(),
            0x12.toByte(), 0x34.toByte(), 0x56.toByte(), 0x78.toByte()
        )
        return Unpooled.wrappedBuffer(magic)
    }

    fun getRealisticMTUSizes(): Array<Int> {
        return arrayOf(1472, 1464, 1200, 1024, 576)
    }

    fun getMinecraftProtocolVersion(): Int {
        return 767
    }

    fun getEnhancedClientConfig(): ClientConfig {
        return ClientConfig(
            guid = generateRealisticClientGUID(),
            protocolVersion = getMinecraftProtocolVersion(),
            mtuSizes = getRealisticMTUSizes(),
            unconnectedMagic = createMinecraftUnconnectedMagic(),
            internalAddresses = generateInternalAddresses(),
            compatibilityMode = true,
            useRealisticTiming = true,
            connectionTimeout = 30000L,
            lan_discovery_enabled = true
        )
    }

    fun getStandardClientConfig(): ClientConfig {
        return ClientConfig(
            guid = generateRealisticClientGUID(),
            protocolVersion = getMinecraftProtocolVersion(),
            mtuSizes = getRealisticMTUSizes(),
            unconnectedMagic = createMinecraftUnconnectedMagic(),
            internalAddresses = generateInternalAddresses(),
            compatibilityMode = true,
            useRealisticTiming = false,
            connectionTimeout = 30000L,
            lan_discovery_enabled = true
        )
    }

    fun getRealisticConnectionDelay(): Long {
        return Random.nextLong(100, 500)
    }

    fun getRealisticSessionTimeout(): Long {
        return Random.nextLong(25000, 35000)
    }

    data class ClientConfig(
        val guid: Long,
        val protocolVersion: Int,
        val mtuSizes: Array<Int>,
        val unconnectedMagic: ByteBuf,
        val internalAddresses: List<String>,
        val compatibilityMode: Boolean,
        val useRealisticTiming: Boolean,
        val connectionTimeout: Long = 30000L,
        val lan_discovery_enabled: Boolean = true
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ClientConfig

            if (guid != other.guid) return false
            if (protocolVersion != other.protocolVersion) return false
            if (!mtuSizes.contentEquals(other.mtuSizes)) return false
            if (unconnectedMagic != other.unconnectedMagic) return false
            if (internalAddresses != other.internalAddresses) return false
            if (compatibilityMode != other.compatibilityMode) return false
            if (useRealisticTiming != other.useRealisticTiming) return false
            if (connectionTimeout != other.connectionTimeout) return false
            if (lan_discovery_enabled != other.lan_discovery_enabled) return false

            return true
        }

        override fun hashCode(): Int {
            var result = guid.hashCode()
            result = 31 * result + protocolVersion
            result = 31 * result + mtuSizes.contentHashCode()
            result = 31 * result + unconnectedMagic.hashCode()
            result = 31 * result + internalAddresses.hashCode()
            result = 31 * result + compatibilityMode.hashCode()
            result = 31 * result + useRealisticTiming.hashCode()
            result = 31 * result + connectionTimeout.hashCode()
            result = 31 * result + lan_discovery_enabled.hashCode()
            return result
        }
    }
}
