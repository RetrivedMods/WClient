package com.retrivedmods.wrelay.codec

import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec

object CodecRegistry {

    private val codecMap = mutableMapOf<Int, BedrockCodec>()
    private val minecraftVersionMap = mutableMapOf<String, Int>()
    private val sortedProtocolVersions = mutableListOf<Int>()

    init {
        registerAllCodecs()
    }

    private fun registerAllCodecs() {
        registerCodec(291, "1.2.0", "org.cloudburstmc.protocol.bedrock.codec.v291.Bedrock_v291")
        registerCodec(313, "1.2.10", "org.cloudburstmc.protocol.bedrock.codec.v313.Bedrock_v313")
        registerCodec(332, "1.4.0", "org.cloudburstmc.protocol.bedrock.codec.v332.Bedrock_v332")
        registerCodec(340, "1.5.0", "org.cloudburstmc.protocol.bedrock.codec.v340.Bedrock_v340")
        registerCodec(354, "1.6.0", "org.cloudburstmc.protocol.bedrock.codec.v354.Bedrock_v354")
        registerCodec(361, "1.7.0", "org.cloudburstmc.protocol.bedrock.codec.v361.Bedrock_v361")
        registerCodec(388, "1.8.0", "org.cloudburstmc.protocol.bedrock.codec.v388.Bedrock_v388")
        registerCodec(389, "1.9.0", "org.cloudburstmc.protocol.bedrock.codec.v389.Bedrock_v389")
        registerCodec(390, "1.10.0", "org.cloudburstmc.protocol.bedrock.codec.v390.Bedrock_v390")
        registerCodec(407, "1.11.0", "org.cloudburstmc.protocol.bedrock.codec.v407.Bedrock_v407")
        registerCodec(408, "1.12.0", "org.cloudburstmc.protocol.bedrock.codec.v408.Bedrock_v408")
        registerCodec(419, "1.13.0", "org.cloudburstmc.protocol.bedrock.codec.v419.Bedrock_v419")
        registerCodec(422, "1.14.0", "org.cloudburstmc.protocol.bedrock.codec.v422.Bedrock_v422")
        registerCodec(428, "1.14.60", "org.cloudburstmc.protocol.bedrock.codec.v428.Bedrock_v428")
        registerCodec(431, "1.15.0", "org.cloudburstmc.protocol.bedrock.codec.v431.Bedrock_v431")
        registerCodec(440, "1.16.0", "org.cloudburstmc.protocol.bedrock.codec.v440.Bedrock_v440")
        registerCodec(448, "1.16.100", "org.cloudburstmc.protocol.bedrock.codec.v448.Bedrock_v448")
        registerCodec(465, "1.16.200", "org.cloudburstmc.protocol.bedrock.codec.v465.Bedrock_v465")
        registerCodec(471, "1.16.210", "org.cloudburstmc.protocol.bedrock.codec.v471.Bedrock_v471")
        registerCodec(475, "1.16.220", "org.cloudburstmc.protocol.bedrock.codec.v475.Bedrock_v475")
        registerCodec(486, "1.17.0", "org.cloudburstmc.protocol.bedrock.codec.v486.Bedrock_v486")
        registerCodec(503, "1.17.30", "org.cloudburstmc.protocol.bedrock.codec.v503.Bedrock_v503")
        registerCodec(527, "1.18.0", "org.cloudburstmc.protocol.bedrock.codec.v527.Bedrock_v527")
        registerCodec(534, "1.18.10", "org.cloudburstmc.protocol.bedrock.codec.v534.Bedrock_v534")
        registerCodec(544, "1.18.30", "org.cloudburstmc.protocol.bedrock.codec.v544.Bedrock_v544")
        registerCodec(545, "1.19.0", "org.cloudburstmc.protocol.bedrock.codec.v545.Bedrock_v545")
        registerCodec(554, "1.19.10", "org.cloudburstmc.protocol.bedrock.codec.v554.Bedrock_v554")
        registerCodec(557, "1.19.20", "org.cloudburstmc.protocol.bedrock.codec.v557.Bedrock_v557")
        registerCodec(560, "1.19.30", "org.cloudburstmc.protocol.bedrock.codec.v560.Bedrock_v560")
        registerCodec(567, "1.19.40", "org.cloudburstmc.protocol.bedrock.codec.v567.Bedrock_v567")
        registerCodec(568, "1.19.50", "org.cloudburstmc.protocol.bedrock.codec.v568.Bedrock_v568")
        registerCodec(575, "1.19.60", "org.cloudburstmc.protocol.bedrock.codec.v575.Bedrock_v575")
        registerCodec(582, "1.19.70", "org.cloudburstmc.protocol.bedrock.codec.v582.Bedrock_v582")
        registerCodec(589, "1.19.80", "org.cloudburstmc.protocol.bedrock.codec.v589.Bedrock_v589")
        registerCodec(594, "1.20.0", "org.cloudburstmc.protocol.bedrock.codec.v594.Bedrock_v594")
        registerCodec(618, "1.20.10", "org.cloudburstmc.protocol.bedrock.codec.v618.Bedrock_v618")
        registerCodec(622, "1.20.30", "org.cloudburstmc.protocol.bedrock.codec.v622.Bedrock_v622")
        registerCodec(630, "1.20.40", "org.cloudburstmc.protocol.bedrock.codec.v630.Bedrock_v630")
        registerCodec(649, "1.20.50", "org.cloudburstmc.protocol.bedrock.codec.v649.Bedrock_v649")
        registerCodec(662, "1.20.60", "org.cloudburstmc.protocol.bedrock.codec.v662.Bedrock_v662")
        registerCodec(671, "1.20.70", "org.cloudburstmc.protocol.bedrock.codec.v671.Bedrock_v671")
        registerCodec(685, "1.20.80", "org.cloudburstmc.protocol.bedrock.codec.v685.Bedrock_v685")
        registerCodec(686, "1.21.0", "org.cloudburstmc.protocol.bedrock.codec.v686.Bedrock_v686")
        registerCodec(712, "1.21.20", "org.cloudburstmc.protocol.bedrock.codec.v712.Bedrock_v712")
        registerCodec(729, "1.21.30", "org.cloudburstmc.protocol.bedrock.codec.v729.Bedrock_v729")
        registerCodec(748, "1.21.40", "org.cloudburstmc.protocol.bedrock.codec.v748.Bedrock_v748")
        registerCodec(766, "1.21.50", "org.cloudburstmc.protocol.bedrock.codec.v766.Bedrock_v766")
        registerCodec(776, "1.21.60", "org.cloudburstmc.protocol.bedrock.codec.v776.Bedrock_v776")
        registerCodec(786, "1.21.70", "org.cloudburstmc.protocol.bedrock.codec.v786.Bedrock_v786")
        registerCodec(800, "1.21.80", "org.cloudburstmc.protocol.bedrock.codec.v800.Bedrock_v800")
        registerCodec(818, "1.21.90", "org.cloudburstmc.protocol.bedrock.codec.v818.Bedrock_v818")
        registerCodec(819, "1.21.93", "org.cloudburstmc.protocol.bedrock.codec.v819.Bedrock_v819")
        registerCodec(827, "1.21.100", "org.cloudburstmc.protocol.bedrock.codec.v827.Bedrock_v827")
        registerCodec(844, "1.21.111", "org.cloudburstmc.protocol.bedrock.codec.v844.Bedrock_v844")
        registerCodec(859, "1.21.120", "org.cloudburstmc.protocol.bedrock.codec.v859.Bedrock_v859")
        registerCodec(860, "1.21.124", "org.cloudburstmc.protocol.bedrock.codec.v860.Bedrock_v860")

        sortedProtocolVersions.sortDescending()
    }

    private fun registerCodec(protocolVersion: Int, minecraftVersion: String, className: String) {
        try {
            val codecClass = Class.forName(className)
            val codecField = codecClass.getDeclaredField("CODEC")
            codecField.isAccessible = true
            val codec = codecField.get(null) as BedrockCodec
            
            codecMap[protocolVersion] = codec
            minecraftVersionMap[minecraftVersion] = protocolVersion
            sortedProtocolVersions.add(protocolVersion)
            
            println("Registered codec: $minecraftVersion (protocol $protocolVersion)")
        } catch (e: Exception) {
            println("Failed to register codec $minecraftVersion: ${e.message}")
        }
    }

    fun getCodecByProtocol(protocolVersion: Int): BedrockCodec? {
        return codecMap[protocolVersion]
    }

    fun getClosestCodec(protocolVersion: Int): BedrockCodec {
        val exactMatch = codecMap[protocolVersion]
        if (exactMatch != null) {
            return exactMatch
        }

        val closestVersion = sortedProtocolVersions.findLast { it <= protocolVersion }
            ?: sortedProtocolVersions.last()

        return codecMap[closestVersion]!!
    }

    fun getLatestCodec(): BedrockCodec {
        return codecMap[sortedProtocolVersions.first()]!!
    }

    fun getMinecraftVersion(protocolVersion: Int): String? {
        return minecraftVersionMap.entries.find { it.value == protocolVersion }?.key
    }

}