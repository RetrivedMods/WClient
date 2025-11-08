package com.retrivedmods.wclient.game.module.visual

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.entity.Entity
import com.retrivedmods.wclient.game.entity.EntityUnknown
import com.retrivedmods.wclient.game.entity.LocalPlayer
import com.retrivedmods.wclient.game.entity.MobList
import com.retrivedmods.wclient.game.entity.Player
import com.retrivedmods.wclient.overlay.hud.EntityType
import com.retrivedmods.wclient.overlay.hud.MinimapEntity
import com.retrivedmods.wclient.overlay.hud.MinimapOverlay
import com.retrivedmods.wclient.overlay.hud.MinimapPosition
import org.cloudburstmc.protocol.bedrock.packet.MoveEntityAbsolutePacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlin.math.PI
import kotlin.math.sqrt

class MinimapModule : Module("minimap", ModuleCategory.Visual) {

    private val showPlayers by boolValue("Show Players", true)
    private val showMobs by boolValue("Show Mobs", false)
    private val showItems by boolValue("Show Items", false)
    private val rangeValue by intValue("Range", 100, 25..500)
    private val sizeOption by intValue("Size", 120, 60..300)
    private val zoomOption by floatValue("Zoom", 1.0f, 0.5f..3.0f)
    private val dotSizeOption by intValue("Dot Size", 3, 1..8)
    private val showNames by boolValue("Show Names", true)
    private val showDistance by boolValue("Show Distance", false)
    private val followPlayer by boolValue("Follow Player", true)
    private val showCoordinates by boolValue("Show Coordinates", false)
    private val transparentBackground by boolValue("Transparent BG", false)

    override fun onEnabled() {
        super.onEnabled()
        try {
            if (isSessionCreated) {
                MinimapOverlay.setOverlayEnabled(true)
                updateMinimapSettings()
            }
        } catch (e: Exception) {
            println("Error enabling Minimap: ${e.message}")
        }
    }

    override fun onDisabled() {
        super.onDisabled()
        if (isSessionCreated) {
            MinimapOverlay.setOverlayEnabled(false)
            MinimapOverlay.setEntities(emptyList())
        }
    }

    override fun onDisconnect(reason: String) {
        if (isSessionCreated) {
            MinimapOverlay.setEntities(emptyList())
        }
    }

    private fun updateMinimapSettings() {
        MinimapOverlay.setMinimapSize(sizeOption.toFloat())
        MinimapOverlay.setMinimapZoom(zoomOption)
        MinimapOverlay.setDotSize(dotSizeOption)
        MinimapOverlay.setShowNames(showNames)
        MinimapOverlay.setShowDistance(showDistance)
        MinimapOverlay.setShowCoordinates(showCoordinates)
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled || !isSessionCreated) return

        val packet = interceptablePacket.packet

        if (packet is PlayerAuthInputPacket) {
            val position = packet.position
            MinimapOverlay.setCenter(position.x, position.z)

            val yawRadians = (packet.rotation.y * PI / 180).toFloat()
            MinimapOverlay.setPlayerRotation(yawRadians)

            updateEntityPositions()
            updateMinimapSettings()
        }

        if (packet is MoveEntityAbsolutePacket) {
            updateEntityPositions()
        }
    }

    private fun updateEntityPositions() {
        val validEntities = searchForValidEntities()
        val minimapEntities = validEntities.map { entity ->
            val position = entity.vec3Position
            val distance = distance2D(entity)
            val entityType = when (entity) {
                is Player -> EntityType.PLAYER
                is EntityUnknown -> when {
                    entity.isMob() -> EntityType.MOB
                    entity.isItem() -> EntityType.ITEM
                    else -> EntityType.MOB
                }
                else -> EntityType.MOB
            }
            val name = when (entity) {
                is Player -> entity.username
                is EntityUnknown -> entity.identifier.substringAfter("minecraft:")
                else -> "Unknown"
            }
            MinimapEntity(
                position = MinimapPosition(position.x, position.z),
                type = entityType,
                name = name,
                distance = distance
            )
        }
        MinimapOverlay.setEntities(minimapEntities)
    }

    private fun Entity.isValidTarget(): Boolean {
        return when (this) {
            is LocalPlayer -> false
            is Player -> {
                showPlayers
            }
            is EntityUnknown -> {
                when {
                    showMobs && this.isMob() -> true
                    showItems && this.isItem() -> true
                    showMobs -> true
                    else -> false
                }
            }
            else -> showMobs
        }
    }

    private fun EntityUnknown.isMob(): Boolean {
        return this.identifier in MobList.mobTypes
    }

    private fun EntityUnknown.isItem(): Boolean {
        return this.identifier == "minecraft:item"
    }

    private fun Player.isBot(): Boolean {
        if (this is LocalPlayer) return false
        return false
    }

    private fun distance2D(entity: Entity): Float {
        val dx = entity.posX - session.localPlayer.posX
        val dz = entity.posZ - session.localPlayer.posZ
        return sqrt(dx * dx + dz * dz)
    }

    private fun searchForValidEntities(): List<Entity> {
        val allEntities = session.level.entityMap.values
        val validEntities = allEntities.filter { entity ->
            val isValid = entity.isValidTarget()
            val distance = distance2D(entity)
            val inRange = distance <= rangeValue
            isValid && inRange
        }.sortedBy { distance2D(it) }

        return validEntities
    }
}
