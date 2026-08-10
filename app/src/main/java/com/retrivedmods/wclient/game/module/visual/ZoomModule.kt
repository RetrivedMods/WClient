package com.retrivedmods.wclient.game.module.visual

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.data.Ability
import org.cloudburstmc.protocol.bedrock.data.AbilityLayer
import org.cloudburstmc.protocol.bedrock.data.PlayerPermission
import org.cloudburstmc.protocol.bedrock.data.command.CommandPermission
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.UpdateAbilitiesPacket

class ZoomModule : Module("zoom", ModuleCategory.Visual) {

    private var zoomLevel by floatValue("zoomLevel", 7.0f, 1.0f..20.0f)
    private var smoothZoom by boolValue("smoothZoom", true)
    private var scrollZoom by boolValue("scrollZoom", false)
    private var showZoomIndicator by boolValue("showIndicator", true)
    private var cinematic by boolValue("cinematic", false)

    // Packet to enable zooming
    private val enableZoomPacket = UpdateAbilitiesPacket().apply {
        playerPermission = PlayerPermission.OPERATOR
        commandPermission = CommandPermission.OWNER
        abilityLayers.add(AbilityLayer().apply {
            layerType = AbilityLayer.Type.BASE
            abilitiesSet.addAll(Ability.entries.toTypedArray())
            abilityValues.addAll(
                arrayOf(
                    Ability.BUILD,
                    Ability.MINE,
                    Ability.DOORS_AND_SWITCHES,
                    Ability.OPEN_CONTAINERS,
                    Ability.ATTACK_PLAYERS,
                    Ability.ATTACK_MOBS,
                    Ability.OPERATOR_COMMANDS
                )
            )
            walkSpeed = zoomLevel
        })
    }

    // Packet to disable zooming
    private val disableZoomPacket = UpdateAbilitiesPacket().apply {
        playerPermission = PlayerPermission.OPERATOR
        commandPermission = CommandPermission.OWNER
        abilityLayers.add(AbilityLayer().apply {
            layerType = AbilityLayer.Type.BASE
            abilitiesSet.addAll(Ability.entries.toTypedArray())
            abilityValues.addAll(
                arrayOf(
                    Ability.BUILD,
                    Ability.MINE,
                    Ability.DOORS_AND_SWITCHES,
                    Ability.OPEN_CONTAINERS,
                    Ability.ATTACK_PLAYERS,
                    Ability.ATTACK_MOBS,
                    Ability.OPERATOR_COMMANDS
                )
            )
            walkSpeed = 0.1f
        })
    }

    private var isZoomEnabled = false

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            if (!isZoomEnabled && isEnabled) {
                // Enable zoom
                enableZoomPacket.uniqueEntityId = session.localPlayer.uniqueEntityId
                session.clientBound(enableZoomPacket)
                isZoomEnabled = true
            } else if (isZoomEnabled && !isEnabled) {
                // Disable zoom
                disableZoomPacket.uniqueEntityId = session.localPlayer.uniqueEntityId
                session.clientBound(disableZoomPacket)
                isZoomEnabled = false
            }
        }
    }
}