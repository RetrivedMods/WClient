package com.retrivedmods.wrelay.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.retrivedmods.wrelay.address.WAddress
import net.raphimc.minecraftauth.bedrock.BedrockAuthManager
import org.jose4j.json.internal.json_simple.JSONObject
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.consumer.JwtConsumerBuilder
import org.jose4j.jwx.HeaderParameterNames
import java.security.KeyFactory
import java.security.interfaces.ECPublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Suppress("SpellCheckingInspection")
object AuthUtils {

    private const val MOJANG_PUBLIC_KEY =
        "MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAECRXueJeTDqNRRgJi/vlRufByu/2G0i2Ebt6YMar5QX/R0DIIyrJMcUpruK4QveTfJSTp3Shlq4Gk34cD/4GUWwkv0DVuzeuB+tXija7HBxii03NHDbPAD0AKnLr2wdAp"

    private var mojangPublicKey = fetchMojangPublicKey()

    val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    // In MinecraftAuth 4.x, FullBedrockSession.mcChain bundled the mojang/identity JWTs together
    // with the ECDSA keypair used to self-sign the third chain link. In 5.x those live in two
    // separate places on BedrockAuthManager: the JWTs come from minecraftCertificateChain, and
    // the keypair is authManager.sessionKeyPair.
    @OptIn(ExperimentalEncodingApi::class)
    fun fetchOnlineChain(authManager: BedrockAuthManager): List<String> {
        val certChain = authManager.minecraftCertificateChain.getUpToDate()
        val sessionKeyPair = authManager.sessionKeyPair

        val publicBase64Key = Base64.encode(sessionKeyPair.public.encoded)
        val consumer = JwtConsumerBuilder()
            .setAllowedClockSkewInSeconds(60)
            .setVerificationKey(mojangPublicKey)
            .build()

        val mojangJws = consumer.process(certChain.mojangJwt).joseObjects[0] as JsonWebSignature

        val claimsSet = JwtClaims()
        claimsSet.setClaim("certificateAuthority", true)
        claimsSet.setClaim("identityPublicKey", mojangJws.getHeader("x5u"))
        claimsSet.setExpirationTimeMinutesInTheFuture((2 * 24 * 60).toFloat()) // 2 days
        claimsSet.setNotBeforeMinutesInThePast(1f)

        val selfSignedJws = JsonWebSignature()
        selfSignedJws.payload = claimsSet.toJson()
        selfSignedJws.key = sessionKeyPair.private
        selfSignedJws.algorithmHeaderValue = "ES384"
        selfSignedJws.setHeader(HeaderParameterNames.X509_URL, publicBase64Key)

        val selfSignedJwt = selfSignedJws.compactSerialization

        return listOf(selfSignedJwt, certChain.mojangJwt, certChain.identityJwt)
    }

    @OptIn(ExperimentalEncodingApi::class, ExperimentalUuidApi::class)
    fun fetchOnlineSkinData(
        authManager: BedrockAuthManager,
        skinData: JSONObject,
        remoteAddress: WAddress
    ): String {
        val certChain = authManager.minecraftCertificateChain.getUpToDate()
        val sessionKeyPair = authManager.sessionKeyPair
        val playFabToken = authManager.playFabToken.getUpToDate()

        val publicKeyBase64 = Base64.encode(sessionKeyPair.public.encoded)

        val overridedData = HashMap<String, Any>()
        overridedData["PlayFabId"] = playFabToken.playFabId.lowercase(Locale.ROOT)
        overridedData["DeviceId"] = Uuid.random().toString()
        overridedData["DeviceOS"] = 1
        overridedData["ThirdPartyName"] = certChain.identityDisplayName
        overridedData["ServerAddress"] = "${remoteAddress.hostName}:${remoteAddress.port}"

        skinData.putAll(overridedData)

        val jws = JsonWebSignature()
        jws.algorithmHeaderValue = "ES384"
        jws.setHeader(HeaderParameterNames.X509_URL, publicKeyBase64)
        jws.payload = skinData.toJSONString()
        jws.key = sessionKeyPair.private

        return jws.compactSerialization
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun fetchMojangPublicKey(): ECPublicKey {
        return KeyFactory.getInstance("EC")
            .generatePublic(X509EncodedKeySpec(Base64.decode(MOJANG_PUBLIC_KEY))) as ECPublicKey
    }

}
