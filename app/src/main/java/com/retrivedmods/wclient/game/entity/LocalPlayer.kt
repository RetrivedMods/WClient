package com.retrivedmods.wclient.game.entity

import com.retrivedmods.wclient.game.GameSession
import com.retrivedmods.wclient.game.inventory.AbstractInventory
import com.retrivedmods.wclient.game.inventory.ContainerInventory
import com.retrivedmods.wclient.game.inventory.PlayerInventory
import com.retrivedmods.wclient.game.registry.BlockDefinition
import com.retrivedmods.wclient.game.utils.misc.removeNetInfo
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.math.vector.Vector3i
import org.cloudburstmc.protocol.bedrock.data.AuthoritativeMovementMode
import org.cloudburstmc.protocol.bedrock.data.SoundEvent
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryActionData
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventorySource
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType
import org.cloudburstmc.protocol.bedrock.packet.AnimatePacket
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.ContainerClosePacket
import org.cloudburstmc.protocol.bedrock.packet.ContainerOpenPacket
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket
import org.cloudburstmc.protocol.bedrock.packet.LevelSoundEventPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket
import java.util.UUID

@Suppress("MemberVisibilityCanBePrivate")
class LocalPlayer(val session: GameSession) : Player(0L, 0L, UUID.randomUUID(), "") {

    override var runtimeEntityId: Long = 0L
        private set

    override var uniqueEntityId: Long = 0L
        private set

    override var uuid: UUID = UUID.randomUUID()
        private set

    var blockBreakServerAuthoritative = false
        private set

    var movementServerAuthoritative = true
        private set

    var inventoriesServerAuthoritative = false
        private set

    var soundServerAuthoritative = false
        private set

    override val inventory = PlayerInventory(this)

    var openContainer: AbstractInventory? = null
        private set

    override fun onPacketBound(packet: BedrockPacket) {
        super.onPacketBound(packet)
        if (packet is StartGamePacket) {
            runtimeEntityId = packet.runtimeEntityId
            uniqueEntityId = packet.uniqueEntityId

            movementServerAuthoritative =
                packet.authoritativeMovementMode != AuthoritativeMovementMode.CLIENT
            packet.authoritativeMovementMode = AuthoritativeMovementMode.SERVER
            inventoriesServerAuthoritative = packet.isInventoriesServerAuthoritative
            blockBreakServerAuthoritative = packet.isServerAuthoritativeBlockBreaking
            soundServerAuthoritative = packet.networkPermissions.isServerAuthSounds

            reset()
        }
        if (packet is PlayerAuthInputPacket) {
            move(packet.position)
            rotate(packet.rotation)
            tickExists = packet.tick
        }
        if (packet is ContainerOpenPacket) {
            openContainer = if (packet.id.toInt() == 0) {
                return
            } else {
                ContainerInventory(packet.id.toInt(), packet.type)
            }
        }
        if (packet is ContainerClosePacket && packet.id.toInt() == openContainer?.containerId) {
            openContainer = null
        }

        inventory.onPacketBound(packet)
        openContainer?.also {
            if (it is ContainerInventory) {
                it.onPacketBound(packet)
            }
        }
    }

    /**
     * Places [definition] at [target] by "clicking" the existing block at [referencePos] from
     * [face] (0=down,1=up,2=north,3=south,4=west,5=east; [target] must equal [referencePos] plus
     * that face's direction - see Level.findPlacementReference, which computes both). Ported from
     * ProtoHax's EntityLocalPlayer.placeBlock.
     *
     * Predicts the placement into our own world tracking immediately (matching real client/server
     * behavior - the server doesn't wait for round-trip confirmation before the block "exists"
     * locally), and - critically - attaches an inventory action for the consumed item when
     * [inventoriesServerAuthoritative] is true, which most modern servers require or they silently
     * drop the whole transaction.
     */
    fun placeBlock(target: Vector3i, referencePos: Vector3i, face: Int, definition: BlockDefinition) {
        session.level.setBlockIdAt(target.x, target.y, target.z, definition.runtimeId)

        val packet = InventoryTransactionPacket().apply {
            transactionType = InventoryTransactionType.ITEM_USE
            actionType = 0
            blockPosition = referencePos
            blockFace = face
            hotbarSlot = inventory.heldItemSlot
            itemInHand = inventory.hand
            playerPosition = vec3Position
            clickPosition = Vector3f.from(
                Math.random().toFloat(),
                Math.random().toFloat(),
                Math.random().toFloat()
            )
            blockDefinition = definition

            if (inventoriesServerAuthoritative) {
                val current = itemInHand
                val afterUse = if (current.count > 1) {
                    current.toBuilder().count(current.count - 1).build()
                } else {
                    ItemData.AIR
                }
                actions.add(
                    InventoryActionData(
                        InventorySource.fromContainerWindowId(0),
                        hotbarSlot,
                        current,
                        afterUse
                    )
                )
            }
        }

        session.serverBound(packet)
    }

    fun swing() {
        val animatePacket = AnimatePacket()
        animatePacket.action = AnimatePacket.Action.SWING_ARM
        animatePacket.runtimeEntityId = runtimeEntityId

        session.serverBound(animatePacket)
        session.clientBound(animatePacket)

        val levelSoundEventPacket = LevelSoundEventPacket()
        levelSoundEventPacket.sound = SoundEvent.ATTACK_NODAMAGE
        levelSoundEventPacket.position = vec3Position
        levelSoundEventPacket.extraData = -1
        levelSoundEventPacket.identifier = "minecraft:player"
        levelSoundEventPacket.isBabySound = false
        levelSoundEventPacket.isRelativeVolumeDisabled = false

        session.serverBound(levelSoundEventPacket)
        session.clientBound(levelSoundEventPacket)
    }

    fun attack(entity: Entity) {
        swing()

        val inventoryTransactionPacket = InventoryTransactionPacket()
        inventoryTransactionPacket.transactionType = InventoryTransactionType.ITEM_USE_ON_ENTITY
        inventoryTransactionPacket.actionType = 1
        inventoryTransactionPacket.runtimeEntityId = entity.runtimeEntityId
        inventoryTransactionPacket.hotbarSlot = inventory.heldItemSlot
        inventoryTransactionPacket.itemInHand = inventory.hand
        inventoryTransactionPacket.playerPosition = vec3Position
        inventoryTransactionPacket.clickPosition = Vector3f.ZERO

        session.serverBound(inventoryTransactionPacket)
    }

    override fun onDisconnect() {
        super.onDisconnect()
        reset()
    }

}