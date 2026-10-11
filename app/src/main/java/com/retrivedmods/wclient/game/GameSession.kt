package com.retrivedmods.wclient.game

import android.util.Log
import com.retrivedmods.wclient.application.AppContext
import com.retrivedmods.wclient.game.entity.LocalPlayer
import com.retrivedmods.wclient.game.registry.BlockMapping
import com.retrivedmods.wclient.game.registry.BlockMappingProvider
import com.retrivedmods.wclient.game.registry.ItemMapping
import com.retrivedmods.wclient.game.registry.ItemMappingProvider
import com.retrivedmods.wclient.game.world.Level
import com.retrivedmods.wclient.game.world.World
import com.retrivedmods.wclient.game.utils.combat.ConsumeTracker
import com.retrivedmods.wclient.game.utils.combat.HitTracker
import com.retrivedmods.wclient.game.utils.combat.LatencyTracker
import com.retrivedmods.wrelay.WRelaySession
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.ItemComponentPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.math.vector.Vector2f
import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket
import org.cloudburstmc.protocol.bedrock.packet.TextPacket
import org.cloudburstmc.protocol.bedrock.packet.TickSyncPacket
import org.cloudburstmc.protocol.common.SimpleDefinitionRegistry

@Suppress("MemberVisibilityCanBePrivate")
class GameSession(val wRelaySession: WRelaySession) : ComposedPacketHandler {

    val localPlayer = LocalPlayer(this)
    val level = Level(this)

    /** Voxel store (block states around the player) — see `game/world/World.kt`. */
    val world = World(this)

    /** Relay <-> server RTT estimate used to size aim prediction. */
    val latency = LatencyTracker(this)

    /** Attack -> HURT feedback loop that auto-tunes the prediction offset. */
    val hitTracker = HitTracker(latency)

    /** Tracks when the local player is eating/drinking (manual or server state). */
    val consumeTracker = ConsumeTracker(this)

    val protocolVersion: Int
        get() = wRelaySession.server.codec.protocolVersion

    /**
     * True when the negotiated protocol still ships the legacy TickSync echo probe. TickSync was
     * deregistered in protocol 685 (1.20.60), so it must not be sent on newer servers: the codec has
     * no id for it and the write would fail inside the encoder.
     */
    val supportsTickSyncProbe: Boolean
        get() = runCatching {
            wRelaySession.server.codec.getPacketDefinition(TickSyncPacket::class.java) != null
        }.getOrDefault(false)

    /** RakNet-level RTT (ms) of the relay <-> server link, or null before it has been measured. */
    fun relayUpstreamPingMs(): Long? = runCatching { wRelaySession.upstreamPingMs }.getOrNull()

    private val mappingProviderContext = AppContext.instance

    private val blockMappingProvider = BlockMappingProvider(mappingProviderContext)
    private val itemMappingProvider = ItemMappingProvider(mappingProviderContext)

    lateinit var blockMapping: BlockMapping
    lateinit var itemMapping: ItemMapping

    /**
     * The MCPEData mappings for this protocol version loaded successfully. False on protocol
     * versions without bundled state files (801+ today) — modules that need block/item names
     * must stay dormant then.
     */
    val mappingsLoaded: Boolean
        get() = ::blockMapping.isInitialized && ::itemMapping.isInitialized

    private var startGameReceived = false

    fun clientBound(packet: BedrockPacket) {
        wRelaySession.clientBound(packet)
    }

    fun serverBound(packet: BedrockPacket) {
        wRelaySession.serverBound(packet)
    }

    // WRelay's beforeClientBound hook receives packets sent by the local game client
    // to the upstream server. beforeServerBound receives packets coming back from the
    // upstream server, so keep InterceptablePacket.serverBound aligned with its
    // documented client -> server meaning.
    override fun beforeServerBound(packet: BedrockPacket): Boolean = handlePacket(packet, false)

    override fun beforeClientBound(packet: BedrockPacket): Boolean = handlePacket(packet, true)

    override fun beforePacketBound(packet: BedrockPacket): Boolean = handlePacket(packet, null)

    private fun handlePacket(packet: BedrockPacket, serverBound: Boolean?): Boolean {
        when (packet) {
            is StartGamePacket -> {
                try {
                    val itemDefinitions = SimpleDefinitionRegistry.builder<ItemDefinition>()
                        .addAll(packet.itemDefinitions)
                        .build()

                    wRelaySession.server.peer.codecHelper.itemDefinitions = itemDefinitions
                    wRelaySession.client?.peer?.codecHelper?.itemDefinitions = itemDefinitions

                    Log.i("GameSession", "Successfully set up codecHelper itemDefinitions: ${packet.itemDefinitions.size} items")
                } catch (e: Exception) {
                    Log.e("GameSession", "Failed to set up codecHelper itemDefinitions", e)
                }

                if (!startGameReceived) {
                    startGameReceived = true
                    Log.i("GameSession", "StartGamePacket received")

                    try {
                        blockMapping = blockMappingProvider.craftMapping(protocolVersion)
                        itemMapping = itemMappingProvider.craftMapping(protocolVersion)

                        Log.i("GameSession", "Loaded mappings for protocol $protocolVersion")
                    } catch (e: Exception) {
                        Log.e("GameSession", "Failed to load mappings for protocol $protocolVersion", e)
                    }
                }
            }

            is ItemComponentPacket -> {
                try {
                    val itemDefinitions = SimpleDefinitionRegistry.builder<ItemDefinition>()
                        .addAll(packet.items)
                        .build()

                    wRelaySession.server.peer.codecHelper.itemDefinitions = itemDefinitions
                    wRelaySession.client?.peer?.codecHelper?.itemDefinitions = itemDefinitions

                    Log.i("GameSession", "Successfully updated codecHelper from ItemComponentPacket: ${packet.items.size} items")
                } catch (e: Exception) {
                    Log.e("GameSession", "Failed to update codecHelper from ItemComponentPacket", e)
                }
            }
        }

        localPlayer.onPacketBound(packet)
        level.onPacketBound(packet)
        world.onPacketBound(packet)

        if (latency.onPacket(packet)) return true
        hitTracker.onPacket(packet)
        consumeTracker.onPacket(packet)
        if (packet is PlayerAuthInputPacket) latency.tick()

        val interceptablePacket = InterceptablePacket(packet, serverBound)

        for (module in ModuleManager.modules) {
            // Refresh modules when reconnecting to a different session.
            if (!module.isSessionCreated || module.session !== this) {
                module.session = this
            }
            module.beforePacketBound(interceptablePacket)
            if (interceptablePacket.isIntercepted) {
                return true
            }
        }

        if (packet is PlayerAuthInputPacket) {
            return applySilentRotation(packet)
        }

        return false
    }

    /**
     * Silent rotations: the relay normally forwards the raw bytes of a packet, so editing the
     * decoded object does nothing. When a combat module requested a rotation we drop the raw
     * packet and re-send the same (re-encoded) packet with only the rotation replaced. The
     * client never receives anything, so the player's camera is untouched.
     */
    private fun applySilentRotation(packet: PlayerAuthInputPacket): Boolean {
        val rotation = localPlayer.silentRotation
        localPlayer.silentRotation = null

        if (rotation == null) {
            localPlayer.onServerRotationSent(packet.rotation)
            return false
        }

        packet.rotation = rotation
        if (packet.interactRotation != null) {
            packet.interactRotation = Vector2f.from(rotation.x, rotation.y)
        }
        localPlayer.onServerRotationSent(rotation)

        serverBound(packet)
        // We intercepted the original, so the relay will not fire the "after" hooks itself.
        afterPacketBound(packet)
        return true
    }

    override fun afterPacketBound(packet: BedrockPacket) {
        for (module in ModuleManager.modules) {
            module.afterPacketBound(packet)
        }
    }

    override fun onDisconnect(reason: String) {
        localPlayer.onDisconnect()
        level.onDisconnect()
        world.onDisconnect()
        latency.reset()
        hitTracker.reset()
        consumeTracker.reset()
        startGameReceived = false

        for (module in ModuleManager.modules) {
            module.onDisconnect(reason)
        }
    }

    fun displayClientMessage(message: String, type: TextPacket.Type = TextPacket.Type.RAW) {
        val textPacket = TextPacket()
        textPacket.type = type
        textPacket.sourceName = ""
        textPacket.message = message
        textPacket.xuid = ""
        textPacket.platformChatId = ""
        textPacket.filteredMessage = ""
        clientBound(textPacket)
    }

}