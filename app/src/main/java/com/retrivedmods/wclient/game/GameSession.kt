package com.retrivedmods.wclient.game

import android.util.Log
import com.retrivedmods.wclient.application.AppContext
import com.retrivedmods.wclient.game.entity.LocalPlayer
import com.retrivedmods.wclient.game.registry.BlockMapping
import com.retrivedmods.wclient.game.registry.BlockMappingProvider
import com.retrivedmods.wclient.game.registry.ItemMapping
import com.retrivedmods.wclient.game.registry.ItemMappingProvider
import com.retrivedmods.wclient.game.world.Level
import com.retrivedmods.wclient.util.setPacketField
import com.retrivedmods.wrelay.WRelaySession
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.ItemComponentPacket
import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket
import org.cloudburstmc.protocol.bedrock.packet.TextPacket
import org.cloudburstmc.protocol.common.SimpleDefinitionRegistry

@Suppress("MemberVisibilityCanBePrivate")
class GameSession(val wRelaySession: WRelaySession) : ComposedPacketHandler {

    val localPlayer = LocalPlayer(this)
    val level = Level(this)

    val protocolVersion: Int
        get() = wRelaySession.server.codec.protocolVersion

    private val mappingProviderContext = AppContext.instance

    private val blockMappingProvider = BlockMappingProvider(mappingProviderContext)
    private val itemMappingProvider = ItemMappingProvider(mappingProviderContext)

    lateinit var blockMapping: BlockMapping
    lateinit var itemMapping: ItemMapping

    // Exposed instead of letting other classes do `session::blockMapping.isInitialized`
    // directly - checking a lateinit property's isInitialized from outside its declaring
    // class can fail to compile ("Backing field ... is not accessible at this point"),
    // so we keep the check here where it's always safe and hand out a plain Boolean.
    val isBlockMappingInitialized: Boolean
        get() = ::blockMapping.isInitialized

    private var startGameReceived = false

    fun clientBound(packet: BedrockPacket) {
        wRelaySession.clientBound(packet)
    }

    fun serverBound(packet: BedrockPacket) {
        wRelaySession.serverBound(packet)
    }

    // ComposedPacketHandler's default beforeClientBound()/beforeServerBound() both just call
    // this single method, so modules previously had no way to tell which direction a packet
    // was going (e.g. AntiCrystalModule rewriting position needs to only touch outgoing/
    // server-bound packets, not incoming ones). We now override beforeClientBound/
    // beforeServerBound below instead, so this is only kept to satisfy the interface and
    // defaults to treating the packet as server-bound if anything else ends up calling it
    // directly.
    override fun beforePacketBound(packet: BedrockPacket): Boolean {
        return handlePacketBound(packet, isClientBound = false)
    }

    override fun beforeClientBound(packet: BedrockPacket): Boolean {
        return handlePacketBound(packet, isClientBound = true)
    }

    override fun beforeServerBound(packet: BedrockPacket): Boolean {
        return handlePacketBound(packet, isClientBound = false)
    }

    private fun handlePacketBound(packet: BedrockPacket, isClientBound: Boolean): Boolean {
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

                    // Prefer the block palette the *server itself* sent in this exact packet over
                    // our bundled per-protocol-version asset files. Those asset files only go up to
                    // an old protocol (they need to be hand-updated every Minecraft version), so on
                    // any server newer than that, block runtime IDs looked up from them are silently
                    // wrong (e.g. asking for "minecraft:obsidian" and getting back some unrelated
                    // block) - which makes every placement attempt get rejected by the server, since
                    // the block it's told to place doesn't match the item actually being used.
                    // The server's own palette is *always* correct for whatever version it's running,
                    // so this can never go stale the way the bundled files do.
                    val livePalette = extractBlockPaletteFromStartGame(packet)
                    if (livePalette != null) {
                        try {
                            blockMapping = BlockMapping.fromPalette(livePalette)
                            Log.i("GameSession", "Loaded block mapping from the server's own StartGamePacket palette (${livePalette.size} entries)")
                        } catch (e: Exception) {
                            Log.e("GameSession", "Failed to build block mapping from StartGamePacket palette, falling back to bundled asset", e)
                        }
                    } else {
                        Log.w("GameSession", "Could not find a block palette on StartGamePacket (tried: getBlockPalette/blockPalette/getBlockProperties/blockProperties) - falling back to the bundled per-protocol asset file, which may be outdated for this server's version")
                    }

                    try {
                        if (!isBlockMappingInitialized) {
                            blockMapping = blockMappingProvider.craftMapping(protocolVersion)
                        }
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

        val interceptablePacket = InterceptablePacket(packet, isClientBound)

        for (module in ModuleManager.modules) {
            // Set session if not already set
            if (!module.isSessionCreated) {
                module.session = this
            }
            module.beforePacketBound(interceptablePacket)
            if (interceptablePacket.isIntercepted) {
                return true
            }
        }

        return false
    }

    override fun afterPacketBound(packet: BedrockPacket) {
        for (module in ModuleManager.modules) {
            module.afterPacketBound(packet)
        }
    }

    override fun onDisconnect(reason: String) {
        localPlayer.onDisconnect()
        level.onDisconnect()
        startGameReceived = false

        for (module in ModuleManager.modules) {
            module.onDisconnect(reason)
        }
    }

    fun displayClientMessage(message: String, type: TextPacket.Type = TextPacket.Type.RAW) {
        val textPacket = TextPacket()
        textPacket.type = type
        textPacket.sourceName = ""
        textPacket.setPacketField("message", message)
        textPacket.xuid = ""
        textPacket.platformChatId = ""
        textPacket.setPacketField("filteredMessage", "")
        clientBound(textPacket)
    }

    /**
     * Reflection-based lookup for StartGamePacket's block palette. Used instead of referencing a
     * property directly (like `packet.itemDefinitions` above) because - unlike itemDefinitions,
     * which is already used elsewhere in this codebase against the current bedrock-codec version
     * and therefore known to exist - the exact accessor name for the block palette on this specific
     * library version hasn't been confirmed against source. Trying several plausible candidates via
     * reflection means a wrong guess here just falls through to the next candidate (or the bundled
     * asset fallback) instead of breaking the build.
     */
    private fun extractBlockPaletteFromStartGame(packet: StartGamePacket): List<org.cloudburstmc.nbt.NbtMap>? {
        val candidates = listOf("getBlockPalette", "blockPalette", "getBlockProperties", "blockProperties")
        for (name in candidates) {
            try {
                val method = packet.javaClass.getMethod(name)
                val result = method.invoke(packet)
                @Suppress("UNCHECKED_CAST")
                val list = result as? List<org.cloudburstmc.nbt.NbtMap> ?: continue
                if (list.isNotEmpty()) return list
            } catch (e: NoSuchMethodException) {
                // try the next candidate name
            } catch (e: Exception) {
                Log.w("GameSession", "Found StartGamePacket.$name() but couldn't read it as a block palette", e)
            }
        }
        return null
    }

}
