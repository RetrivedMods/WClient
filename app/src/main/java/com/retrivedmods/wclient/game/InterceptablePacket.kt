package com.retrivedmods.wclient.game

import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket

data class InterceptablePacket(
    val packet: BedrockPacket,
    // true = server -> client (about to reach the game client), false = client -> server
    // (about to reach the real server). Modules that only make sense in one direction - e.g.
    // AntiCrystalModule rewriting the position we report to the server - need this to avoid
    // acting on packets going the wrong way.
    val isClientBound: Boolean
) {

    var isIntercepted = false
        private set

    fun intercept() {
        isIntercepted = true
    }

}
