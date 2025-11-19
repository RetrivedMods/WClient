package com.retrivedmods.wclient.game


import android.content.Context
import android.net.Uri
import com.retrivedmods.wclient.application.AppContext
import com.retrivedmods.wclient.game.module.combat.AntiCrystalModule
import com.retrivedmods.wclient.game.module.combat.AntiKnockbackModule
import com.retrivedmods.wclient.game.module.combat.AutoArmorModule
import com.retrivedmods.wclient.game.module.combat.CrystalSmashModule
import com.retrivedmods.wclient.game.module.combat.EnemyHunterModule
import com.retrivedmods.wclient.game.module.combat.HitAndRunModule
import com.retrivedmods.wclient.game.module.combat.HitboxModule
import com.retrivedmods.wclient.game.module.combat.KillauraModule
import com.retrivedmods.wclient.game.module.combat.TriggerBotModule
import com.retrivedmods.wclient.game.module.combat.WAuraModule
import com.retrivedmods.wclient.game.module.misc.AdvanceDisablerModule

import com.retrivedmods.wclient.game.module.misc.AutoDisconnectModule
import com.retrivedmods.wclient.game.module.misc.BaritoneModule
import com.retrivedmods.wclient.game.module.misc.CommandHandlerModule
import com.retrivedmods.wclient.game.module.visual.CoordinatesModule
import com.retrivedmods.wclient.game.module.misc.DesyncModule
import com.retrivedmods.wclient.game.module.misc.FakeDeathModule
import com.retrivedmods.wclient.game.module.misc.FakeXPModule

import com.retrivedmods.wclient.game.module.misc.MinerModule
import com.retrivedmods.wclient.game.module.misc.NoChatModule
import com.retrivedmods.wclient.game.module.motion.NoClipModule
import com.retrivedmods.wclient.game.module.misc.PieChartModule
import com.retrivedmods.wclient.game.module.misc.PositionLoggerModule
import com.retrivedmods.wclient.game.module.misc.ReplayModule

import com.retrivedmods.wclient.game.module.misc.ChestStealerModule
import com.retrivedmods.wclient.game.module.world.AntiDebuffModule
import com.retrivedmods.wclient.game.module.world.EffectsModule
import com.retrivedmods.wclient.game.module.world.ParticlesModule
import com.retrivedmods.wclient.game.module.world.TimeShiftModule
import com.retrivedmods.wclient.game.module.world.WeatherControllerModule
import com.retrivedmods.wclient.game.module.motion.AirJumpModule
import com.retrivedmods.wclient.game.module.motion.AntiAFKModule
import com.retrivedmods.wclient.game.module.motion.AutoWalkModule
import com.retrivedmods.wclient.game.module.motion.BhopModule
import com.retrivedmods.wclient.game.module.motion.FlyModule
import com.retrivedmods.wclient.game.module.motion.HighJumpModule
import com.retrivedmods.wclient.game.module.motion.JetPackModule
import com.retrivedmods.wclient.game.module.motion.MotionFlyModule
import com.retrivedmods.wclient.game.module.motion.PlayerTPModule
import com.retrivedmods.wclient.game.module.motion.SpeedModule
import com.retrivedmods.wclient.game.module.motion.SpiderModule
import com.retrivedmods.wclient.game.module.motion.SprintModule
import com.retrivedmods.wclient.game.module.motion.UnifiedFlyModule
import com.retrivedmods.wclient.game.module.visual.CrosshairModule
import com.retrivedmods.wclient.game.module.visual.DamageTextModule
import com.retrivedmods.wclient.game.module.visual.ESPModule
import com.retrivedmods.wclient.game.module.visual.FullbrightModule
import com.retrivedmods.wclient.game.module.visual.MinimapModule
import com.retrivedmods.wclient.game.module.visual.NetworkInfoModule
import com.retrivedmods.wclient.game.module.visual.NoHurtCameraModule
import com.retrivedmods.wclient.game.module.visual.PlayerJoinModule
import com.retrivedmods.wclient.game.module.visual.SpeedDisplayModule
import com.retrivedmods.wclient.game.module.visual.WorldStateModule
import com.retrivedmods.wclient.game.module.visual.ZoomModule
import com.retrivedmods.wclient.game.module.visual.TargetHudModule
import com.retrivedmods.wclient.game.module.world.FreeCameraModule
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import java.io.File

object ModuleManager {

    private val _modules: MutableList<Module> = ArrayList()

    val modules: List<Module> = _modules

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    init {
        with(_modules) {
            // Combat
            add(WAuraModule())
            add(KillauraModule())
            add(EnemyHunterModule())
            add(AntiKnockbackModule())
            add(AutoArmorModule())
            add(AntiCrystalModule())
            add(HitAndRunModule())
            add(HitboxModule())
            add(CrystalSmashModule())
            add(TriggerBotModule())

            
            // Motion

            add(MotionFlyModule())
            add(PlayerTPModule())

            add(FlyModule())
            add(SpeedModule())
            add(AirJumpModule())
            add(NoClipModule())
            add(JetPackModule())
            add(HighJumpModule())
            add(BhopModule())
            add(SprintModule())
            add(AutoWalkModule())
            add(AntiAFKModule())
            add(SpiderModule())
            add(UnifiedFlyModule())
            
            // Visual

            add(DamageTextModule())
            add(ESPModule())
            add(PlayerJoinModule())
            add(ZoomModule())
            add(CoordinatesModule())
            add(NoHurtCameraModule())
            add(SpeedDisplayModule())
            add(NetworkInfoModule())
            add(WorldStateModule())
            add(MinimapModule())
            add(CrosshairModule())
            add(TargetHudModule())
            add(FullbrightModule())
            
            // World
            add(FreeCameraModule())
            add(TimeShiftModule())
            add(WeatherControllerModule())
            add(EffectsModule())
            add(ParticlesModule())
            add(AntiDebuffModule())

            // Misc
            add(AutoDisconnectModule())
            add(ChestStealerModule())
            add(DesyncModule())
            add(PositionLoggerModule())
            add(NoChatModule())
            add(AdvanceDisablerModule())
            add(CommandHandlerModule())
            add(ReplayModule())
            add(BaritoneModule())
            add(PieChartModule())
            add(FakeDeathModule())
            add(FakeXPModule())
            add(MinerModule())
        }
    }

    fun saveConfig() {
        val configsDir = AppContext.instance.filesDir.resolve("configs")
        configsDir.mkdirs()

        val config = configsDir.resolve("UserConfig.json")
        val jsonObject = buildJsonObject {
            put("modules", buildJsonObject {
                _modules.forEach {
                    if (it.private) {
                        return@forEach
                    }
                    put(it.name, it.toJson())
                }
            })
        }

        config.writeText(json.encodeToString(jsonObject))
    }

    fun loadConfig() {
        val configsDir = AppContext.instance.filesDir.resolve("configs")
        configsDir.mkdirs()

        val config = configsDir.resolve("UserConfig.json")
        if (!config.exists()) {
            return
        }

        val jsonString = config.readText()
        if (jsonString.isEmpty()) {
            return
        }

        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        val modules = jsonObject["modules"]!!.jsonObject
        _modules.forEach { module ->
            (modules[module.name] as? JsonObject)?.let {
                module.fromJson(it)
            }
        }
    }

    fun exportConfig(): String {
        val jsonObject = buildJsonObject {
            put("modules", buildJsonObject {
                _modules.forEach {
                    if (it.private) {
                        return@forEach
                    }
                    put(it.name, it.toJson())
                }
            })
        }
        return json.encodeToString(jsonObject)
    }

    fun importConfig(configStr: String) {
        try {
            val jsonObject = json.parseToJsonElement(configStr).jsonObject
            val modules = jsonObject["modules"]?.jsonObject ?: return

            _modules.forEach { module ->
                modules[module.name]?.let {
                    if (it is JsonObject) {
                        module.fromJson(it)
                    }
                }
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid config format")
        }
    }

    fun exportConfigToFile(context: Context, fileName: String): Boolean {
        return try {
            val configsDir = context.getExternalFilesDir("configs")
            configsDir?.mkdirs()

            val configFile = File(configsDir, "$fileName.json")
            configFile.writeText(exportConfig())
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun importConfigFromFile(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val configStr = input.bufferedReader().readText()
                importConfig(configStr)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}