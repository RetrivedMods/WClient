package com.retrivedmods.wclient.game


import com.retrivedmods.wclient.game.module.combat.CriticModule
import com.retrivedmods.wclient.game.module.misc.CrasherModule
import android.content.Context
import android.net.Uri
import com.retrivedmods.wclient.application.AppContext
import com.retrivedmods.wclient.game.module.combat.AdvanceCombatAuraModule
import com.retrivedmods.wclient.game.module.combat.WAuraModule
import com.retrivedmods.wclient.game.module.combat.AntiCrystalModule
import com.retrivedmods.wclient.game.module.combat.HitboxModule
import com.retrivedmods.wclient.game.module.combat.TrollerModule
import com.retrivedmods.wclient.game.module.combat.InfiniteAuraModule
import com.retrivedmods.wclient.game.module.combat.AntiKnockbackModule
import com.retrivedmods.wclient.game.module.combat.AutoclickerModule
import com.retrivedmods.wclient.game.module.combat.CrystalauraModule
import com.retrivedmods.wclient.game.module.combat.EnemyHunterModule
import com.retrivedmods.wclient.game.module.combat.KillauraModule
import com.retrivedmods.wclient.game.module.combat.RotationAuraModule
import com.retrivedmods.wclient.game.module.combat.SlotSwitcherModule
import com.retrivedmods.wclient.game.module.misc.AdvanceDisablerModule
import com.retrivedmods.wclient.game.module.misc.AutoDisconnectModule
import com.retrivedmods.wclient.game.module.misc.DesyncModule
import com.retrivedmods.wclient.game.module.motion.NoClipModule
import com.retrivedmods.wclient.game.module.misc.PlayerTracerModule
import com.retrivedmods.wclient.game.module.misc.PositionLoggerModule
import com.retrivedmods.wclient.game.module.misc.TimeShiftModule
import com.retrivedmods.wclient.game.module.misc.BlinkModule
import com.retrivedmods.wclient.game.module.misc.ChatSuffixModule
import com.retrivedmods.wclient.game.module.misc.RegenerationModule
import com.retrivedmods.wclient.game.module.misc.WeatherControllerModule
import com.retrivedmods.wclient.game.module.motion.AirJumpModule
import com.retrivedmods.wclient.game.module.motion.AntiAFKModule
import com.retrivedmods.wclient.game.module.motion.AutoWalkModule
import com.retrivedmods.wclient.game.module.motion.BhopModule
import com.retrivedmods.wclient.game.module.motion.FastStopModule
import com.retrivedmods.wclient.game.module.motion.FlyModule
import com.retrivedmods.wclient.game.module.motion.HighJumpModule
import com.retrivedmods.wclient.game.module.motion.JetPackModule
import com.retrivedmods.wclient.game.module.motion.MotionFlyModule
import com.retrivedmods.wclient.game.module.motion.MotionVarModule
import com.retrivedmods.wclient.game.module.motion.OpFightBotModule
import com.retrivedmods.wclient.game.module.motion.SpeedModule
import com.retrivedmods.wclient.game.module.motion.SprintModule
import com.retrivedmods.wclient.game.module.visual.FreeCameraModule
import com.retrivedmods.wclient.game.module.visual.NoHurtCameraModule
import com.retrivedmods.wclient.game.module.visual.ZoomModule
import com.retrivedmods.wclient.game.module.visual.DamageTextModule
import com.retrivedmods.wclient.game.module.visual.ESPModule
import com.retrivedmods.wclient.game.module.misc.ModAlertModule
import com.retrivedmods.wclient.game.module.misc.SpammerModule
import com.retrivedmods.wclient.game.module.motion.AutoPathModule
import com.retrivedmods.wclient.game.module.motion.TPContainerModule
import com.retrivedmods.wclient.game.module.visual.NightVisionModule
import com.retrivedmods.wclient.game.module.visual.FakeProxyModule
import com.retrivedmods.wclient.game.module.visual.PlayerJoinNotifierModule
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
            add(FlyModule())
            add(ZoomModule())
            add(AirJumpModule())
            add(NoClipModule())
            add(AdvanceCombatAuraModule())
            add(AutoclickerModule())
            add(CrystalauraModule())
            add(TrollerModule())
            add(CriticModule())
            add(CrasherModule())
            add(DamageTextModule())
            add(WAuraModule())
            add(SpeedModule())
            add(JetPackModule())
            add(ESPModule())
            add(BlinkModule())
            add(AdvanceDisablerModule())
            add(RotationAuraModule())
            add(NightVisionModule())
            add(RegenerationModule())
            add(AutoDisconnectModule())
            add(PlayerJoinNotifierModule())
            add(HitboxModule())
            add(InfiniteAuraModule())
            add(ModAlertModule())
            add(EnemyHunterModule())
            add(ChatSuffixModule())
            add(FakeProxyModule())
            add(SpammerModule())
            add(AutoPathModule())
            add(SlotSwitcherModule())
            add(TPContainerModule())

            add(HighJumpModule())

            add(AntiKnockbackModule())
            add(FastStopModule())
            add(OpFightBotModule())

            add(BhopModule())
            add(SprintModule())
            add(NoHurtCameraModule())
            add(AutoWalkModule())
            add(AntiAFKModule())
            add(DesyncModule())
            add(PositionLoggerModule())
            add(MotionFlyModule())
            add(FreeCameraModule())
            add(KillauraModule())
            // add(CriticModule())

            add(AntiCrystalModule())

            add(TimeShiftModule())
            add(WeatherControllerModule())
            //  add(CrasherModule())
            add(MotionVarModule())
            add(PlayerTracerModule())

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