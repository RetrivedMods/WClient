package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.inventory.ContainerInventory
import com.retrivedmods.wclient.game.inventory.PlayerInventory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerType
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData
import org.cloudburstmc.protocol.bedrock.packet.ContainerClosePacket
import org.cloudburstmc.protocol.bedrock.packet.ContainerOpenPacket
import org.cloudburstmc.protocol.bedrock.packet.InventoryContentPacket
import org.cloudburstmc.protocol.bedrock.packet.InventorySlotPacket
import kotlin.random.Random

class ChestStealerModule : Module("ChestStealer", ModuleCategory.Misc) {
    companion object {



        private val SKYWARS_WEAPONS = setOf(
            "minecraft:diamond_sword", "minecraft:iron_sword", "minecraft:stone_sword",
            "minecraft:netherite_sword", "minecraft:golden_sword", "minecraft:wooden_sword",
            "minecraft:bow", "minecraft:crossbow", "minecraft:trident"
        )

        private val SKYWARS_ARMOR = setOf(

            "minecraft:diamond_helmet", "minecraft:diamond_chestplate",
            "minecraft:diamond_leggings", "minecraft:diamond_boots",

            "minecraft:iron_helmet", "minecraft:iron_chestplate",
            "minecraft:iron_leggings", "minecraft:iron_boots",

            "minecraft:chainmail_helmet", "minecraft:chainmail_chestplate",
            "minecraft:chainmail_leggings", "minecraft:chainmail_boots",

            "minecraft:golden_helmet", "minecraft:golden_chestplate",
            "minecraft:golden_leggings", "minecraft:golden_boots",

            "minecraft:leather_helmet", "minecraft:leather_chestplate",
            "minecraft:leather_leggings", "minecraft:leather_boots",

            "minecraft:netherite_helmet", "minecraft:netherite_chestplate",
            "minecraft:netherite_leggings", "minecraft:netherite_boots"
        )

        private val SKYWARS_FOOD = setOf(
            "minecraft:golden_apple", "minecraft:enchanted_golden_apple",
            "minecraft:cooked_beef", "minecraft:cooked_porkchop", "minecraft:cooked_chicken",
            "minecraft:steak", "minecraft:bread", "minecraft:apple", "minecraft:cookie",
            "minecraft:cooked_mutton", "minecraft:cooked_salmon", "minecraft:cooked_cod",
            "minecraft:baked_potato", "minecraft:carrot", "minecraft:golden_carrot"
        )

        private val SKYWARS_BLOCKS = setOf(
            "minecraft:wool", "minecraft:planks", "minecraft:oak_planks",
            "minecraft:spruce_planks", "minecraft:birch_planks", "minecraft:jungle_planks",
            "minecraft:acacia_planks", "minecraft:dark_oak_planks", "minecraft:cobblestone",
            "minecraft:stone", "minecraft:sandstone", "minecraft:endstone",
            "minecraft:obsidian", "minecraft:netherrack", "minecraft:glass"
        )

        private val SKYWARS_PROJECTILES = setOf(
            "minecraft:arrow", "minecraft:spectral_arrow", "minecraft:tipped_arrow",
            "minecraft:snowball", "minecraft:egg", "minecraft:ender_pearl"
        )

        private val SKYWARS_TOOLS = setOf(
            "minecraft:diamond_pickaxe", "minecraft:iron_pickaxe", "minecraft:stone_pickaxe",
            "minecraft:diamond_axe", "minecraft:iron_axe", "minecraft:stone_axe",
            "minecraft:shears", "minecraft:flint_and_steel"
        )

        private val SKYWARS_POTIONS = setOf(
            "minecraft:potion", "minecraft:splash_potion", "minecraft:lingering_potion"
        )

        private val SKYWARS_SPECIAL = setOf(
            "minecraft:totem_of_undying", "minecraft:elytra", "minecraft:shield",
            "minecraft:enchanted_book", "minecraft:experience_bottle",
            "minecraft:tnt", "minecraft:flint_and_steel", "minecraft:lava_bucket",
            "minecraft:water_bucket", "minecraft:fishing_rod"
        )

        private val JUNK_ITEMS = setOf(
            "minecraft:dirt", "minecraft:grass", "minecraft:gravel", "minecraft:sand",
            "minecraft:stick", "minecraft:bowl", "minecraft:string", "minecraft:feather",
            "minecraft:leather", "minecraft:rotten_flesh", "minecraft:bone",
            "minecraft:spider_eye", "minecraft:gunpowder"
        )
    }


    private val autoClose by boolValue("Auto Close", true)
    private val delayMin by intValue("Min Delay (ms)", 0, 0..100)
    private val delayMax by intValue("Max Delay (ms)", 5, 0..100)
    private val startDelay by intValue("Start Delay (ms)", 20, 0..200)
    private val autoEquipArmor by boolValue("Auto Equip Armor", true)
    private val priorityMode by boolValue("Priority Mode", true)
    private val ignoreJunk by boolValue("Ignore Junk", true)
    private val smartSteal by boolValue("Smart Steal", true)
    private val autoSort by boolValue("Auto Sort", false)
    private val showStats by boolValue("Show Stats", true)
    private val stealDelay by intValue("Steal Delay", 1, 0..50)
    private val maxItemsPerChest by intValue("Max Items", 27, 1..54)
    private val onlyValuable by boolValue("Only Valuables", false)
    private val filterMode by enumValue("Filter", FilterMode.WHITELIST, FilterMode::class.java)
    
    enum class FilterMode { WHITELIST, BLACKLIST, ALL }


    private var currentContainer: ContainerInventory? = null
    private var inventoryLoaded = false
    private var stealingJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private var totalItemsStolen = 0
    private var totalContainersProcessed = 0

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet

        try {
            when (packet) {
                is ContainerOpenPacket -> {
                    if (isValidChestContainer(packet.type)) {
                        handleChestOpen(packet)
                    }
                }

                is InventoryContentPacket -> {
                    val container = currentContainer
                    if (container != null && packet.containerId == container.containerId) {
                        try {
                            container.onPacketBound(packet)
                            inventoryLoaded = true

                            if (stealingJob == null || stealingJob?.isActive == false) {
                                stealingJob = coroutineScope.launch {
                                    delay(startDelay.toLong())
                                    if (currentContainer != null && inventoryLoaded && isEnabled && isSessionCreated) {
                                        startStealing()
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // Ignore
                        }
                    }
                }

                is InventorySlotPacket -> {
                    val container = currentContainer
                    if (container != null && packet.containerId == container.containerId) {
                        try {
                            if (packet.slot < container.content.size) {
                                container.content[packet.slot] = packet.item
                            }
                        } catch (e: Exception) {
                            // Ignore
                        }
                    }
                }

                is ContainerClosePacket -> {
                    if (currentContainer != null && packet.id.toInt() == currentContainer!!.containerId) {
                        cleanup()
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun isValidChestContainer(type: ContainerType): Boolean {
        return when (type) {
            ContainerType.CONTAINER, ContainerType.MINECART_CHEST,
            ContainerType.CHEST_BOAT, ContainerType.HOPPER,
            ContainerType.DISPENSER, ContainerType.DROPPER -> true
            else -> false
        }
    }

    private fun handleChestOpen(packet: ContainerOpenPacket) {
        cleanup()
        currentContainer = ContainerInventory(packet.id.toInt(), packet.type)
        inventoryLoaded = false
    }

    private fun startStealing() {
        if (!isSessionCreated) return
        val container = currentContainer ?: return
        if (!inventoryLoaded) return

        val startTime = System.currentTimeMillis()

        stealingJob = coroutineScope.launch {
            try {
                val itemsStolen = stealAllItems(container)
                totalItemsStolen += itemsStolen

                val totalTime = System.currentTimeMillis() - startTime
                totalContainersProcessed++


                if (autoEquipArmor && isSessionCreated && itemsStolen > 0) {
                    delay(50)
                    equipBestArmor()
                }

                if (autoClose) {
                    delay(30)
                    closeChest()
                }
            } catch (e: Exception) {
                cleanup()
            }
        }
    }

    private suspend fun stealAllItems(container: ContainerInventory): Int {
        if (!isSessionCreated) return 0

        val playerInventory = session.localPlayer.inventory
        var totalStolen = 0


        val itemsToSteal = mutableListOf<Triple<Int, ItemData, Int>>()

        for (containerSlot in container.content.indices) {
            val item = container.content[containerSlot]
            if (item == null || item == ItemData.AIR || item.count <= 0) continue

            val priority = getItemPriority(item)
            if (priority > 0) {
                itemsToSteal.add(Triple(containerSlot, item, priority))
            }
        }


        if (priorityMode) {
            itemsToSteal.sortByDescending { it.third }
        }


        for ((containerSlot, item, priority) in itemsToSteal) {
            if (!isEnabled) break

            val emptySlots = playerInventory.content.count { it == ItemData.AIR }
            if (emptySlots == 0) {
                break
            }

            val targetSlot = findTargetSlot(item, playerInventory)
            if (targetSlot == null) continue

            try {
                val requestId = if (session.localPlayer.inventoriesServerAuthoritative) {
                    playerInventory.getRequestId()
                } else {
                    Int.MAX_VALUE
                }

                val movePacket = container.moveItem(
                    containerSlot,
                    targetSlot,
                    playerInventory,
                    requestId
                )

                session.serverBound(movePacket)
                totalStolen++


                if (delayMax > 0) {
                    delay(Random.nextLong(delayMin.toLong(), delayMax.toLong()))
                }

            } catch (e: Exception) {
                continue
            }
        }

        return totalStolen
    }

    private fun getItemPriority(item: ItemData): Int {
        val identifier = item.definition?.identifier ?: return 0


        if (JUNK_ITEMS.contains(identifier)) return 0


        return when {
            SKYWARS_WEAPONS.contains(identifier) -> 1000
            SKYWARS_ARMOR.contains(identifier) -> 900
            SKYWARS_SPECIAL.contains(identifier) -> 850
            SKYWARS_FOOD.contains(identifier) -> 800
            SKYWARS_PROJECTILES.contains(identifier) -> 750
            SKYWARS_TOOLS.contains(identifier) -> 700
            SKYWARS_POTIONS.contains(identifier) -> 650
            SKYWARS_BLOCKS.contains(identifier) -> 600
            item.tag != null && item.tag.toString().contains("ench", ignoreCase = true) -> 950
            else -> 0
        }
    }

    private suspend fun equipBestArmor() {
        if (!isSessionCreated) return

        val playerInventory = session.localPlayer.inventory


        val armorSlots = mapOf(
            "helmet" to 36,
            "chestplate" to 37,
            "leggings" to 38,
            "boots" to 39
        )

        for ((armorType, armorSlotIndex) in armorSlots) {
            try {
                val currentArmor = playerInventory.content.getOrNull(armorSlotIndex)
                val currentTier = getArmorTier(currentArmor)

                var bestSlot = -1
                var bestTier = currentTier

                for (slot in 0 until 36) {
                    val item = playerInventory.content.getOrNull(slot) ?: continue
                    if (item == ItemData.AIR) continue

                    val identifier = item.definition?.identifier ?: continue

                    if (identifier.contains(armorType, ignoreCase = true)) {
                        val tier = getArmorTier(item)

                        if (tier > bestTier) {
                            bestTier = tier
                            bestSlot = slot
                        }
                    }
                }

                if (bestSlot != -1) {
                    try {
                        val requestId = if (session.localPlayer.inventoriesServerAuthoritative) {
                            playerInventory.getRequestId()
                        } else {
                            Int.MAX_VALUE
                        }

                        val movePacket = playerInventory.moveItem(
                            bestSlot,
                            armorSlotIndex,
                            playerInventory,
                            requestId
                        )

                        session.serverBound(movePacket)

                        if (delayMax > 0) {
                            delay(Random.nextLong(delayMin.toLong(), delayMax.toLong()))
                        }
                    } catch (e: Exception) {
                    }
                }

            } catch (e: Exception) {

            }
        }
    }

    private fun getArmorTier(item: ItemData?): Int {
        if (item == null || item == ItemData.AIR) return 0

        val identifier = item.definition?.identifier ?: return 0

        val baseTier = when {
            identifier.contains("netherite") -> 5
            identifier.contains("diamond") -> 4
            identifier.contains("iron") -> 3
            identifier.contains("chainmail") -> 2
            identifier.contains("golden") -> 2
            identifier.contains("leather") -> 1
            else -> 0
        }

        return try {
            val hasEnchants = item.tag?.toString()?.contains("ench", ignoreCase = true) == true
            if (hasEnchants) baseTier + 1 else baseTier
        } catch (e: Exception) {
            baseTier
        }
    }

    private fun findTargetSlot(item: ItemData, playerInventory: PlayerInventory): Int? {
        val identifier = item.definition?.identifier


        if (identifier != null) {
            for (i in 9 until 36) {
                val invItem = playerInventory.content[i]
                if (invItem != ItemData.AIR &&
                    invItem.definition?.identifier == identifier &&
                    invItem.count < 64) {
                    return i
                }
            }

            for (i in 0 until 9) {
                val invItem = playerInventory.content[i]
                if (invItem != ItemData.AIR &&
                    invItem.definition?.identifier == identifier &&
                    invItem.count < 64) {
                    return i
                }
            }
        }


        for (i in 9 until 36) {
            if (playerInventory.content[i] == ItemData.AIR) {
                return i
            }
        }


        for (i in 0 until 9) {
            if (playerInventory.content[i] == ItemData.AIR) {
                return i
            }
        }

        return null
    }

    private suspend fun closeChest() {
        val container = currentContainer ?: return

        try {
            val closePacket = ContainerClosePacket().apply {
                id = container.containerId.toByte()
                type = container.type
            }

            session.serverBound(closePacket)
            delay(30)
        } catch (e: Exception) {

        } finally {
            cleanup()
        }
    }

    private fun cleanup() {
        stealingJob?.cancel()
        stealingJob = null
        currentContainer = null
        inventoryLoaded = false
    }

    override fun onEnabled() {
        super.onEnabled()
        totalItemsStolen = 0
        totalContainersProcessed = 0
    }

    override fun onDisabled() {
        super.onDisabled()
        cleanup()

        totalItemsStolen = 0
        totalContainersProcessed = 0
    }
}