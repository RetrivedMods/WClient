package com.retrivedmods.wrelay.listener

import com.retrivedmods.wrelay.WRelaySession
import com.retrivedmods.wrelay.util.AuthUtils
import com.retrivedmods.wrelay.util.refresh
import net.kyori.adventure.text.Component
import net.raphimc.minecraftauth.step.bedrock.session.StepFullBedrockSession
import org.cloudburstmc.protocol.bedrock.data.PacketCompressionAlgorithm
import org.cloudburstmc.protocol.bedrock.data.auth.AuthType
import org.cloudburstmc.protocol.bedrock.data.auth.CertificateChainPayload
import org.cloudburstmc.protocol.bedrock.packet.*
import org.cloudburstmc.protocol.bedrock.util.EncryptionUtils
import org.cloudburstmc.protocol.bedrock.util.JsonUtils
import org.jose4j.json.JsonUtil
import org.jose4j.json.internal.json_simple.JSONObject
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwx.HeaderParameterNames
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


@Suppress("MemberVisibilityCanBePrivate")
class OnlineLoginPacketListener(
    val wRelaySession: WRelaySession,
    private var fullBedrockSession: StepFullBedrockSession.FullBedrockSession
) : WRelayPacketListener {

    private var skinData: JSONObject? = null

    override fun beforeClientBound(packet: BedrockPacket): Boolean {
        if (packet is LoginPacket) {
            if (fullBedrockSession.isExpired) {
                println("Session expired, attempting to refresh tokens...")

                try {
                    fullBedrockSession = fullBedrockSession.refresh()
                    println("Successfully refreshed session for: ${fullBedrockSession.mcChain.displayName}")
                } catch (e: Exception) {
                    println("Failed to refresh session: ${e.message}")
                    e.printStackTrace()
                    wRelaySession.server.disconnect("Your session has expired and could not be refreshed. Please re-login in the W Client.")
                    return true
                }
            }

            println("Processing login packet")

            try {
                val jws = JsonWebSignature()
                jws.compactSerialization = packet.clientJwt

                skinData = JSONObject(JsonUtil.parseJson(jws.unverifiedPayload))
                connectServer()
            } catch (e: Exception) {
                println("Failed to process login packet: ${e.message}")
                e.printStackTrace()
                wRelaySession.server.disconnect("Failed to process login: ${e.message}")
                return true
            }
            return true
        }
        return false
    }

    @OptIn(ExperimentalEncodingApi::class)
    override fun beforeServerBound(packet: BedrockPacket): Boolean {
        if (packet is NetworkSettingsPacket) {
            val threshold = packet.compressionThreshold
            if (threshold > 0) {
                wRelaySession.client!!.setCompression(packet.compressionAlgorithm)
                println("Compression enabled: ${packet.compressionAlgorithm}, threshold: $threshold")
            } else {
                wRelaySession.client!!.setCompression(PacketCompressionAlgorithm.NONE)
                println("Compression disabled")
            }

            try {
                val chain = AuthUtils.fetchOnlineChain(fullBedrockSession)
                val skinData =
                    AuthUtils.fetchOnlineSkinData(
                        fullBedrockSession,
                        skinData!!,
                        wRelaySession.wRelay.remoteAddress!!
                    )

                val loginPacket = LoginPacket()
                loginPacket.protocolVersion = wRelaySession.server.codec.protocolVersion
                loginPacket.authPayload = CertificateChainPayload(chain, AuthType.FULL)
                loginPacket.clientJwt = skinData
                wRelaySession.serverBoundImmediately(loginPacket)

                println("Login packet sent successfully")
            } catch (e: Throwable) {
                println("Login failed: ${e.message}")
                e.printStackTrace()
                wRelaySession.server.disconnect("Authentication failed: ${e.message}")
                return true
            }

            return true
        }
        if (packet is ServerToClientHandshakePacket) {
            try {
                val parts = packet.jwt.split(".")
                if (parts.size != 3) {
                    throw Exception("Invalid JWT format")
                }
                
                val headerJson = String(java.util.Base64.getUrlDecoder().decode(parts[0]))
                val payloadJson = String(java.util.Base64.getUrlDecoder().decode(parts[1]))
                
                val header = JSONObject(JsonUtil.parseJson(headerJson))
                val payload = JSONObject(JsonUtil.parseJson(payloadJson))
                
                val x5u = header.get("x5u") as? String ?: throw Exception("Missing x5u in header")
                val serverKey = EncryptionUtils.parseKey(x5u)
                
                val saltString = payload.get("salt") as? String ?: throw Exception("Missing salt in payload")
                val salt = java.util.Base64.getDecoder().decode(saltString)
                
                val key = EncryptionUtils.getSecretKey(
                    fullBedrockSession.mcChain.privateKey, 
                    serverKey,
                    salt
                )
                
                wRelaySession.client!!.enableEncryption(key)
                println("Encryption enabled successfully")

                wRelaySession.serverBoundImmediately(ClientToServerHandshakePacket())
            } catch (e: Exception) {
                println("Handshake failed: ${e.message}")
                e.printStackTrace()
                wRelaySession.server.disconnect("Handshake failed: ${e.message}")
                return true
            }
            return true
        }
        return false
    }

    private fun connectServer() {
        try {
            wRelaySession.wRelay.connectToServer {
                println("Connected to server, sending network settings request")

                try {
                    val packet = RequestNetworkSettingsPacket()
                    packet.protocolVersion = wRelaySession.server.codec.protocolVersion
                    wRelaySession.serverBoundImmediately(packet)
                    println("Network settings request sent")
                } catch (e: Exception) {
                    println("Failed to send network settings request: ${e.message}")
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            println("Failed to connect to server: ${e.message}")
            e.printStackTrace()
            wRelaySession.server.disconnect("Failed to connect to server: ${e.message}")
        }
    }

}