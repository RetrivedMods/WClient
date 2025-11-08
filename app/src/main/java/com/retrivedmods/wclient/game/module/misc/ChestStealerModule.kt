package com.retrivedmods.wclient.game.module.misc

import android.util.Log
import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.inventory.ContainerInventory
import com.retrivedmods.wclient.game.inventory.PlayerInventory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerType
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData
import org.cloudburstmc.protocol.bedrock.packet.ContainerClosePacket
import org.cloudburstmc.protocol.bedrock.packet.ContainerOpenPacket
import org.cloudburstmc.protocol.bedrock.packet.InventoryContentPacket
import org.cloudburstmc.protocol.bedrock.packet.NetworkStackLatencyPacket
import java.util.ArrayDeque
import kotlin.random.Random

class ChestStealerModule : Module("ChestStealer", ModuleCategory.World) {
    companion object {
        private const val TAG = "ChestStealer"
    }

    // Configuration options
    private val autoClose by boolValue("Auto Close", true)
    private val useDelay by boolValue("Use Delay", true)
    private val minDelay by intValue("Min Delay (ms)", 95, 0..1000)
    private val maxDelay by intValue("Max Delay (ms)", 103, 0..1000)
    private val stealMode by enumValue("Steal Mode", StealMode.ALL_ITEMS, StealMode::class.java)
    private val ignoreJunk by boolValue("Ignore Junk", false)
    private val maxItemsPerTick by intValue("Max Items Per Tick", 10, 1..10)
    private val silentMode by boolValue("Silent Mode", false)
    private val prioritizeValuables by boolValue("Prioritize Valuables", false)
    private val skipFullStacks by boolValue("Skip Full Stacks", false)
    private val containerTimeout by intValue("Container Timeout (ms)", 1000, 1000..30000)
    private var currentContainer: ContainerInventory? = null
    private var isStealingInProgress = false
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var currentLatency = 100L

    private var totalItemsStolen = 0
    private var totalContainersProcessed = 0
    private var lastStealTime = 0L
    private val latencyHistory = ArrayDeque<Long>(10)
    private var lastLatencyUpdate = 0L

    // Item filtering
    private val valuableItems = setOf(
        "minecraft:diamond", "minecraft:emerald", "minecraft:gold_ingot", "minecraft:iron_ingot",
        "minecraft:diamond_sword", "minecraft:diamond_pickaxe", "minecraft:diamond_axe",
        "minecraft:diamond_shovel", "minecraft:diamond_hoe", "minecraft:diamond_helmet",
        "minecraft:diamond_chestplate", "minecraft:diamond_leggings", "minecraft:diamond_boots",
        "minecraft:netherite_sword", "minecraft:netherite_pickaxe", "minecraft:netherite_axe",
        "minecraft:netherite_shovel", "minecraft:netherite_hoe", "minecraft:netherite_helmet",
        "minecraft:netherite_chestplate", "minecraft:netherite_leggings", "minecraft:netherite_boots",
        "minecraft:enchanted_book", "minecraft:totem_of_undying", "minecraft:elytra",
        "minecraft:shulker_shell", "minecraft:nether_star", "minecraft:beacon"
    )

    private val junkItems = setOf(
        "minecraft:dirt", "minecraft:cobblestone", "minecraft:stone", "minecraft:gravel",
        "minecraft:sand", "minecraft:wooden_sword", "minecraft:wooden_pickaxe", "minecraft:wooden_axe",
        "minecraft:wooden_shovel", "minecraft:wooden_hoe", "minecraft:leather_helmet",
        "minecraft:leather_chestplate", "minecraft:leather_leggings", "minecraft:leather_boots",
        "minecraft:rotten_flesh", "minecraft:spider_eye", "minecraft:poisonous_potato",
        "minecraft:stick", "minecraft:bowl", "minecraft:seeds", "minecraft:wheat_seeds"
    )

    private val foodItems = setOf(
        "minecraft:bread", "minecraft:apple", "minecraft:golden_apple", "minecraft:enchanted_golden_apple",
        "minecraft:cooked_beef", "minecraft:cooked_porkchop", "minecraft:cooked_chicken", "minecraft:cooked_salmon",
        "minecraft:cooked_cod", "minecraft:baked_potato", "minecraft:cookie", "minecraft:cake"
    )

    private val toolItems = setOf(
        "minecraft:iron_sword", "minecraft:iron_pickaxe", "minecraft:iron_axe", "minecraft:iron_shovel",
        "minecraft:golden_sword", "minecraft:golden_pickaxe", "minecraft:golden_axe", "minecraft:golden_shovel",
        "minecraft:stone_sword", "minecraft:stone_pickaxe", "minecraft:stone_axe", "minecraft:stone_shovel"
    )

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        when (packet) {
            is NetworkStackLatencyPacket -> {
                if (packet.fromServer) {
                    handleLatencyPacket(packet)
                }
            }
            is ContainerOpenPacket -> {
                if (isValidChestContainer(packet.type)) {
                    handleChestOpen(packet)
                }
            }
            is InventoryContentPacket -> {
                if (currentContainer != null && packet.containerId == currentContainer!!.containerId) {
                    Log.d(TAG, "Received InventoryContentPacket for our container ${packet.containerId}")
                    currentContainer!!.onPacketBound(packet)
                    if (!isStealingInProgress) {
                        Log.d(TAG, "Starting stealing process with delay...")
                        coroutineScope.launch {
                            delay(50)
                            if (!isStealingInProgress) {
                                startStealing()
                            }
                        }
                    } else {
                        Log.d(TAG, "Stealing already in progress, ignoring packet")
                    }
                }
            }
            is ContainerClosePacket -> {
                if (currentContainer != null && packet.id.toInt() == currentContainer!!.containerId) {
                    cleanup()
                }
            }
        }
    }

    private fun handleLatencyPacket(packet: NetworkStackLatencyPacket) {
        if (!useDelay) return

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastLatencyUpdate > 1000) {
            val latency = (currentTime - (packet.timestamp / 1_000_000)).coerceAtLeast(1L)
            latencyHistory.offer(latency)
            if (latencyHistory.size > 10) {
                latencyHistory.poll()
            }
            currentLatency = if (latencyHistory.isEmpty()) {
                100L
            } else {
                latencyHistory.average().toLong()
            }
            lastLatencyUpdate = currentTime
        }
    }

    private fun isValidChestContainer(type: ContainerType): Boolean {
        return when (type) {
            ContainerType.CONTAINER,
            ContainerType.MINECART_CHEST,
            ContainerType.CHEST_BOAT,
            ContainerType.HOPPER,
            ContainerType.DISPENSER,
            ContainerType.DROPPER -> true
            else -> {
                Log.d(TAG, "Unknown container type: $type")
                false
            }
        }
    }

    private fun handleChestOpen(packet: ContainerOpenPacket) {
        Log.d(TAG, "Container opened: ID=${packet.id}, Type=${packet.type}")
        currentContainer = ContainerInventory(packet.id.toInt(), packet.type)
        isStealingInProgress = false
    }

    private fun startStealing() {
        if (isStealingInProgress || currentContainer == null) {
            Log.d(TAG, "Cannot start stealing - inProgress: $isStealingInProgress, container: ${currentContainer != null}")
            return
        }

        if (!isEnabled) {
            Log.d(TAG, "ChestStealer is not enabled, skipping")
            return
        }

        Log.d(TAG, "Starting stealing process - autoClose: $autoClose, useDelay: $useDelay, mode: $stealMode")
        isStealingInProgress = true
        lastStealTime = System.currentTimeMillis()
        coroutineScope.launch {
            try {
                stealAllItems()
                if (autoClose) {
                    Log.d(TAG, "Auto-closing chest")
                    closeChest()
                } else {
                    Log.d(TAG, "Not auto-closing chest")
                    isStealingInProgress = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during stealing", e)
                isStealingInProgress = false
                cleanup()
            }
        }
    }

    private suspend fun stealAllItems() {
        val container = currentContainer ?: return
        val playerInventory = session.localPlayer.inventory
        var itemsStolen = 0
        var itemsSkipped = 0

        Log.d(TAG, "Starting to steal from container with ${container.content.size} slots")

        container.content.forEachIndexed { index, item ->
            Log.d(TAG, "Slot $index: ${item.definition?.identifier ?: "AIR"} x${item.count} (valid: ${item.isValid}) (isAir: ${item == ItemData.AIR})")
        }

        Log.d(TAG, "Container content array size: ${container.content.size}")
        Log.d(TAG, "Container content is empty: ${container.content.isEmpty()}")
        Log.d(TAG, "Container content all AIR: ${container.content.all { it == ItemData.AIR }}")

        val slotsToProcess = if (prioritizeValuables) {
            val slotPriorities = mutableListOf<Pair<Int, Int>>()

            container.content.forEachIndexed { index, item ->
                if (shouldStealItem(item)) {
                    val priority = getItemPriority(item)
                    slotPriorities.add(index to priority)
                }
            }

            val sortedSlots = slotPriorities.sortedByDescending { it.second }.map { it.first }

            Log.d(TAG, "Prioritized processing: ${sortedSlots.size} slots sorted by priority")
            sortedSlots
        } else {
            container.content.indices.filter { shouldStealItem(container.content[it]) }
        }

        Log.d(TAG, "Starting main stealing loop for ${slotsToProcess.size} slots")

        var itemsProcessedThisTick = 0
        val startTime = System.currentTimeMillis()

        for (slot in slotsToProcess) {
            if (System.currentTimeMillis() - startTime > containerTimeout) {
                Log.d(TAG, "Container timeout reached, stopping")
                break
            }
            Log.d(TAG, "Processing slot $slot")

            if (!isEnabled || (!isStealingInProgress && autoClose)) {
                Log.d(TAG, "Stopping steal loop - enabled: $isEnabled, stealing: $isStealingInProgress, autoClose: $autoClose")
                break
            }

            val item = container.content[slot]
            Log.d(TAG, "Slot $slot item: ${item.definition?.identifier ?: "AIR"} x${item.count} valid:${item.isValid} isAir:${item == ItemData.AIR}")

            if (shouldStealItem(item)) {
                Log.d(TAG, "Item in slot $slot passed validation!")
                Log.d(TAG, "Found item in slot $slot: ${item.definition?.identifier ?: "unknown"} x${item.count}")

                val emptySlot = findBestSlotForItem(item, playerInventory)
                Log.d(TAG, "Player inventory slot search result: $emptySlot")

                for (i in 0 until minOf(9, playerInventory.content.size)) {
                    val invItem = playerInventory.content[i]
                    if (invItem != ItemData.AIR) {
                        Log.d(TAG, "Player slot $i: ${invItem.definition?.identifier ?: "unknown"} x${invItem.count}")
                    } else {
                        Log.d(TAG, "Player slot $i: AIR")
                    }
                }

                if (emptySlot != null) {
                    Log.d(TAG, "Found empty slot $emptySlot in player inventory")
                    try {
                        Log.d(TAG, "Attempting to move item from container slot $slot to player slot $emptySlot")
                        Log.d(TAG, "Server authoritative inventories: ${session.localPlayer.inventoriesServerAuthoritative}")

                        val originalContainerItem = container.content[slot]
                        val originalPlayerItem = playerInventory.content[emptySlot]

                        Log.d(TAG, "Original container item: ${originalContainerItem.definition?.identifier ?: "unknown"} x${originalContainerItem.count}")
                        Log.d(TAG, "Original player item: ${originalPlayerItem.definition?.identifier ?: "AIR"}")

                        val movePacket = container.moveItem(
                            slot, emptySlot, playerInventory,
                            if (session.localPlayer.inventoriesServerAuthoritative) session.localPlayer.inventory.getRequestId() else Int.MAX_VALUE
                        )

                        session.serverBound(movePacket)
                        Log.d(TAG, "Sent move packet directly to server")

                        val responseDelay = if (useDelay) calculateDelay().coerceAtMost(500L) else 200L
                        delay(responseDelay)

                        val containerSlotAfterMove = container.content[slot]
                        val playerSlotAfterMove = playerInventory.content[emptySlot]

                        Log.d(TAG, "After move - container item: ${containerSlotAfterMove.definition?.identifier ?: "AIR"} x${containerSlotAfterMove.count}")
                        Log.d(TAG, "After move - player item: ${playerSlotAfterMove.definition?.identifier ?: "AIR"} x${playerSlotAfterMove.count}")

                        val moveSuccessful = (containerSlotAfterMove == ItemData.AIR || containerSlotAfterMove.count < item.count) &&
                                (playerSlotAfterMove.definition?.identifier == item.definition?.identifier)

                        if (moveSuccessful) {
                            itemsStolen++
                            itemsProcessedThisTick++
                            Log.d(TAG, "Successfully moved item from slot $slot to player slot $emptySlot")
                        } else {
                            itemsSkipped++
                            Log.d(TAG, "Move failed for slot $slot - container: ${containerSlotAfterMove.definition?.identifier ?: "AIR"}, player: ${playerSlotAfterMove.definition?.identifier ?: "AIR"}")
                        }

                        if (itemsProcessedThisTick >= maxItemsPerTick) {
                            Log.d(TAG, "Reached max items per tick ($maxItemsPerTick), adding delay")
                            if (useDelay) {
                                val delay = calculateDelay()
                                delay(delay)
                            }
                            itemsProcessedThisTick = 0
                        } else if (useDelay) {
                            delay(25L)
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to move item from slot $slot", e)
                        itemsSkipped++
                        continue
                    }
                } else {
                    Log.d(TAG, "No empty slots available in player inventory, stopping")
                    break
                }
            } else {
                if (item == ItemData.AIR) {
                    Log.d(TAG, "Skipping slot $slot: item is AIR")
                } else if (item.count <= 0) {
                    Log.d(TAG, "Skipping slot $slot: item count is ${item.count}")
                    itemsSkipped++
                } else if (!item.isValid) {
                    Log.d(TAG, "Skipping slot $slot: item is not valid (${item.definition?.identifier ?: "unknown"})")
                    itemsSkipped++
                } else {
                    Log.d(TAG, "Skipping slot $slot: unknown reason (${item.definition?.identifier ?: "unknown"} x${item.count} valid:${item.isValid})")
                    itemsSkipped++
                }
            }
        }

        val stealTime = System.currentTimeMillis() - lastStealTime
        totalItemsStolen += itemsStolen
        totalContainersProcessed++

        Log.d(TAG, "Stealing completed: $itemsStolen stolen, $itemsSkipped skipped in ${stealTime}ms")
        Log.d(TAG, "Total stats: $totalItemsStolen items from $totalContainersProcessed containers")

        if (!silentMode && itemsStolen > 0) {
            session.displayClientMessage("§l§b[ChestStealer] §r§7Stole §a$itemsStolen §7items in §e${stealTime}ms")
        }
    }

    private fun shouldStealItem(item: ItemData): Boolean {
        if (item == ItemData.AIR || item.count <= 0 || !item.isValid) {
            return false
        }

        val identifier = item.definition?.identifier ?: return false

        return when (stealMode) {
            StealMode.ALL_ITEMS -> {
                if (ignoreJunk) !junkItems.contains(identifier) else true
            }
            StealMode.VALUABLE_ONLY -> {
                valuableItems.contains(identifier)
            }
            StealMode.TOOLS_AND_WEAPONS -> {
                toolItems.contains(identifier) || valuableItems.contains(identifier)
            }
            StealMode.FOOD_ONLY -> {
                foodItems.contains(identifier)
            }
            StealMode.NO_JUNK -> {
                !junkItems.contains(identifier)
            }
            StealMode.CUSTOM_FILTER -> {
                valuableItems.contains(identifier) || toolItems.contains(identifier) || foodItems.contains(identifier)
            }
        }
    }

    private fun calculateDelay(): Long {
        val baseDelay = Random.nextLong(minDelay.toLong(), maxDelay.toLong())
        val latencyFactor = if (currentLatency > 0) (currentLatency * 0.3).toLong() else 0L
        return (baseDelay + latencyFactor).coerceAtLeast(10L)
    }

    private fun findBestSlotForItem(item: ItemData, playerInventory: PlayerInventory): Int? {
        val identifier = item.definition?.identifier

        if (!skipFullStacks && identifier != null) {
            for (i in 0 until 36) {
                val invItem = playerInventory.content[i]
                if (invItem.definition?.identifier == identifier &&
                    invItem.count < 64 &&
                    invItem.count + item.count <= 64) {
                    Log.d(TAG, "Found stackable slot $i for ${identifier}")
                    return i
                }
            }
        }

        return playerInventory.findEmptySlot()
    }

    private fun getItemPriority(item: ItemData): Int {
        val identifier = item.definition?.identifier ?: return 0

        return when {
            valuableItems.contains(identifier) -> 100
            toolItems.contains(identifier) -> 50
            foodItems.contains(identifier) -> 30
            junkItems.contains(identifier) -> 1
            else -> 10
        }
    }

    private suspend fun closeChest() {
        val container = currentContainer ?: return
        try {
            val closePacket = ContainerClosePacket().apply {
                id = container.containerId.toByte()
                isServerInitiated = false
                type = container.type
            }
            session.serverBound(closePacket)
            Log.d(TAG, "Chest closed automatically")
        } finally {
            cleanup()
        }
    }

    private fun cleanup() {
        currentContainer = null
        isStealingInProgress = false
    }

    private fun resetLatencyTracking() {
        currentLatency = 100L
        latencyHistory.clear()
        lastLatencyUpdate = 0L
    }

    private fun resetStatistics() {
        totalItemsStolen = 0
        totalContainersProcessed = 0
        lastStealTime = 0L
    }

    override fun onEnabled() {
        super.onEnabled()
        resetStatistics()
        if (!silentMode && isSessionCreated) {
            session.displayClientMessage("§l§b[ChestStealer] §r§7Mode: §a${stealMode.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }}")
        }
    }

    override fun onDisabled() {
        super.onDisabled()
        cleanup()
        resetLatencyTracking()

        if (!silentMode && isSessionCreated && totalContainersProcessed > 0) {
            session.displayClientMessage("§l§b[ChestStealer] §r§7Final Stats: §a$totalItemsStolen §7items from §e$totalContainersProcessed §7containers")
        }

        resetStatistics()
    }

    enum class StealMode {
        ALL_ITEMS,
        VALUABLE_ONLY,
        TOOLS_AND_WEAPONS,
        FOOD_ONLY,
        NO_JUNK,
        CUSTOM_FILTER
    }
}