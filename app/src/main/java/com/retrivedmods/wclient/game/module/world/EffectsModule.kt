package com.retrivedmods.wclient.game.module.world

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.data.Effect
import org.cloudburstmc.protocol.bedrock.packet.MobEffectPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class EffectsModule : Module("effects", ModuleCategory.World) {

    private val absorption by boolValue("Absorption", false)
    private val badOmen by boolValue("Bad Omen", false)
    private val blindness by boolValue("Blindness", false)
    private val conduitPower by boolValue("Conduit Power", false)
    private val darkness by boolValue("Darkness", false)
    private val fatalPoison by boolValue("Fatal Poison", false)
    private val fireResistance by boolValue("Fire Resistance", false)
    private val healthBoost by boolValue("Health Boost", false)
    private val hunger by boolValue("Hunger", false)
    private val instantDamage by boolValue("Instant Damage", false)
    private val instantHealth by boolValue("Instant Health", false)
    private val invisibility by boolValue("Invisibility", false)
    private val jumpBoost by boolValue("Jump Boost", false)
    private val levitation by boolValue("Levitation", false)
    private val nausea by boolValue("Nausea", false)
    private val poison by boolValue("Poison", false)
    private val regeneration by boolValue("Regeneration", false)
    private val resistance by boolValue("Resistance", false)
    private val saturation by boolValue("Saturation", false)
    private val slowFalling by boolValue("Slow Falling", false)
    private val strength by boolValue("Strength", false)
    private val swiftness by boolValue("Swiftness", false)
    private val villageHero by boolValue("Village Hero", false)
    private val weakness by boolValue("Weakness", false)
    private val wither by boolValue("Wither", false)
    private val waterBreathing by boolValue("Water Breathing", false)
    private val nightVision by boolValue("Night Vision", false)

    private val amplifier by intValue("Amplifier", 1, 1..5)

    override fun onDisabled() {
        super.onDisabled()
        if (isSessionCreated) {
            removeAllEffects()
        }
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            if (session.localPlayer.tickExists % 20 == 0L) {
                applyEffects()
            }
        }
    }

    private fun applyEffects() {
        if (absorption) applyEffect(Effect.ABSORPTION)
        if (badOmen) applyEffect(Effect.BAD_OMEN)
        if (blindness) applyEffect(Effect.BLINDNESS)
        if (conduitPower) applyEffect(Effect.CONDUIT_POWER)
        if (darkness) applyEffect(Effect.DARKNESS)
        if (fatalPoison) applyEffect(Effect.FATAL_POISON)
        if (fireResistance) applyEffect(Effect.FIRE_RESISTANCE)
        if (healthBoost) applyEffect(Effect.HEALTH_BOOST)
        if (hunger) applyEffect(Effect.HUNGER)
        if (instantDamage) applyEffect(Effect.INSTANT_DAMAGE)
        if (instantHealth) applyEffect(Effect.INSTANT_HEALTH)
        if (invisibility) applyEffect(Effect.INVISIBILITY)
        if (jumpBoost) applyEffect(Effect.JUMP_BOOST)
        if (levitation) applyEffect(Effect.LEVITATION)
        if (nausea) applyEffect(Effect.NAUSEA)
        if (poison) applyEffect(Effect.POISON)
        if (regeneration) applyEffect(Effect.REGENERATION)
        if (resistance) applyEffect(Effect.RESISTANCE)
        if (saturation) applyEffect(Effect.SATURATION)
        if (slowFalling) applyEffect(Effect.SLOW_FALLING)
        if (strength) applyEffect(Effect.STRENGTH)
        if (swiftness) applyEffect(Effect.SWIFTNESS)
        if (villageHero) applyEffect(Effect.VILLAGE_HERO)
        if (weakness) applyEffect(Effect.WEAKNESS)
        if (wither) applyEffect(Effect.WITHER)
        if (waterBreathing) applyEffect(Effect.WATER_BREATHING)
        if (nightVision) applyEffect(Effect.NIGHT_VISION)
        if (regeneration) applyEffect(Effect.REGENERATION)
    }

    private fun applyEffect(effectId: Int) {
        session.clientBound(MobEffectPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            event = MobEffectPacket.Event.ADD
            this.effectId = effectId
            this.amplifier = this@EffectsModule.amplifier - 1
            isParticles = false
            duration = 360000
        })
    }

    private fun removeAllEffects() {
        val effects = listOf(
            Effect.ABSORPTION, Effect.BAD_OMEN, Effect.BLINDNESS, Effect.CONDUIT_POWER,
            Effect.DARKNESS, Effect.FATAL_POISON, Effect.FIRE_RESISTANCE, Effect.HEALTH_BOOST,
            Effect.HUNGER, Effect.INSTANT_DAMAGE, Effect.INSTANT_HEALTH, Effect.INVISIBILITY,
            Effect.JUMP_BOOST, Effect.LEVITATION, Effect.NAUSEA, Effect.POISON,
            Effect.REGENERATION, Effect.RESISTANCE, Effect.SATURATION, Effect.SLOW_FALLING,
            Effect.STRENGTH, Effect.SWIFTNESS, Effect.VILLAGE_HERO, Effect.WEAKNESS, Effect.WITHER,
            Effect.WATER_BREATHING, Effect.NIGHT_VISION
        )

        effects.forEach { effectId ->
            session.clientBound(MobEffectPacket().apply {
                runtimeEntityId = session.localPlayer.runtimeEntityId
                event = MobEffectPacket.Event.REMOVE
                this.effectId = effectId
            })
        }
    }
}
