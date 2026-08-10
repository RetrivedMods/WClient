package com.retrivedmods.wclient.game

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.retrivedmods.wclient.application.AppContext
import com.retrivedmods.wclient.game.module.combat.ACAModule
import com.retrivedmods.wclient.game.module.combat.AdvanceCombatAuraModule
import com.retrivedmods.wclient.game.module.combat.AntiCrystalModule
import com.retrivedmods.wclient.game.module.combat.AntiKnockbackModule
import com.retrivedmods.wclient.game.module.combat.AutoArmorModule
import com.retrivedmods.wclient.game.module.combat.AutoSoupModule
import com.retrivedmods.wclient.game.module.combat.AutoWeaponModule
import com.retrivedmods.wclient.game.module.combat.BlockClutchModule
import com.retrivedmods.wclient.game.module.combat.BowAimbotModule
import com.retrivedmods.wclient.game.module.combat.CriticalsModule
import com.retrivedmods.wclient.game.module.combat.CrystalSmashModule
import com.retrivedmods.wclient.game.module.combat.EnemyHunterModule
import com.retrivedmods.wclient.game.module.combat.HitAndRunModule
import com.retrivedmods.wclient.game.module.combat.HitboxModule
import com.retrivedmods.wclient.game.module.combat.KillauraModule
import com.retrivedmods.wclient.game.module.combat.TriggerBotModule
import com.retrivedmods.wclient.game.module.combat.WAuraModule
import com.retrivedmods.wclient.game.module.combat.AutoFightModule
import com.retrivedmods.wclient.game.module.combat.AutoHvHModule
import com.retrivedmods.wclient.game.module.combat.AutoTotemModule
import com.retrivedmods.wclient.game.module.combat.HotbarSwitcherModule
import com.retrivedmods.wclient.game.module.combat.InfiniteAuraModule
import com.retrivedmods.wclient.game.module.combat.SuperKnockbackModule
import com.retrivedmods.wclient.game.module.combat.VelocityModule
import com.retrivedmods.wclient.game.module.combat.WTapModule
import com.retrivedmods.wclient.game.module.misc.ArrayListModule
import com.retrivedmods.wclient.game.module.misc.AutoToolModule
import com.retrivedmods.wclient.game.module.misc.DerpModule
import com.retrivedmods.wclient.game.module.misc.FastPlaceModule
import com.retrivedmods.wclient.game.module.misc.InventoryCleanerModule
import com.retrivedmods.wclient.game.module.misc.MCFModule
import com.retrivedmods.wclient.game.module.misc.NoFallModule
import com.retrivedmods.wclient.game.module.misc.NoRotateModule
import com.retrivedmods.wclient.game.module.misc.PingSpoofModule
import com.retrivedmods.wclient.game.module.misc.TimerModule
import com.retrivedmods.wclient.game.module.motion.AutoSprintModule
import com.retrivedmods.wclient.game.module.motion.BlinkModule
import com.retrivedmods.wclient.game.module.motion.ClickTPModule
import com.retrivedmods.wclient.game.module.motion.DashModule
import com.retrivedmods.wclient.game.module.motion.GlideModule
import com.retrivedmods.wclient.game.module.motion.IceSpeedModule
import com.retrivedmods.wclient.game.module.motion.InvWalkModule
import com.retrivedmods.wclient.game.module.motion.LongJumpModule
import com.retrivedmods.wclient.game.module.motion.NoClipModule
import com.retrivedmods.wclient.game.module.world.AutoBuilderModule
import com.retrivedmods.wclient.game.module.misc.AutoDisconnectModule
import com.retrivedmods.wclient.game.module.misc.CommandHandlerModule
import com.retrivedmods.wclient.game.module.visual.AmbienceModule
import com.retrivedmods.wclient.game.module.visual.AnimationsModule
import com.retrivedmods.wclient.game.module.visual.ChamsModule
import com.retrivedmods.wclient.game.module.visual.ChunkBordersModule
import com.retrivedmods.wclient.game.module.visual.ClearWaterModule
import com.retrivedmods.wclient.game.module.visual.CoordinatesModule
import com.retrivedmods.wclient.game.module.misc.DesyncModule
import com.retrivedmods.wclient.game.module.misc.FakeDeathModule
import com.retrivedmods.wclient.game.module.misc.FakeXPModule
import com.retrivedmods.wclient.game.module.misc.MinerModule
import com.retrivedmods.wclient.game.module.misc.NoChatModule
import com.retrivedmods.wclient.game.module.misc.PieChartModule
import com.retrivedmods.wclient.game.module.misc.PositionLoggerModule
import com.retrivedmods.wclient.game.module.misc.ReplayModule
import com.retrivedmods.wclient.game.module.visual.FOVSliderModule
import com.retrivedmods.wclient.game.module.visual.ItemESPModule
import com.retrivedmods.wclient.game.module.visual.NameTagsModule
import com.retrivedmods.wclient.game.module.visual.NoWeatherModule
import com.retrivedmods.wclient.game.module.visual.TracersModule
import com.retrivedmods.wclient.game.module.misc.ChestStealerModule
import com.retrivedmods.wclient.game.module.misc.SpammerModule
import com.retrivedmods.wclient.game.module.misc.ToggleSoundModule
import com.retrivedmods.wclient.game.module.misc.WaterMarkModule
import com.retrivedmods.wclient.game.module.motion.StepModule
import com.retrivedmods.wclient.game.module.motion.WallClimbModule
import com.retrivedmods.wclient.game.module.visual.TargetHudModule
import com.retrivedmods.wclient.game.module.world.AntiDebuffModule
import com.retrivedmods.wclient.game.module.world.AutoFarmModule
import com.retrivedmods.wclient.game.module.world.AutoFishModule
import com.retrivedmods.wclient.game.module.world.AutoMineModule
import com.retrivedmods.wclient.game.module.world.ChestESPModule
import com.retrivedmods.wclient.game.module.world.FastBreakModule
import com.retrivedmods.wclient.game.module.world.NukerModule
import com.retrivedmods.wclient.game.module.world.PortalClickerModule
import com.retrivedmods.wclient.game.module.world.ScaffoldModule
import com.retrivedmods.wclient.game.module.world.XRayModule
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
import com.retrivedmods.wclient.game.module.world.FreeCameraModule
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
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
            add(HotbarSwitcherModule())
            add(KillauraModule())
            add(AutoFightModule())
            add(InfiniteAuraModule())
            add(ACAModule())
            add(AutoTotemModule())
            add(AutoHvHModule())
            add(EnemyHunterModule())
            add(AntiKnockbackModule())
            add(AntiCrystalModule())
            add(HitAndRunModule())
            add(HitboxModule())
            add(CrystalSmashModule())
            add(TriggerBotModule())
            add(AutoArmorModule())
            add(BowAimbotModule())
            add(CriticalsModule())
            add(SuperKnockbackModule())
            add(VelocityModule())
            add(WTapModule())
            add(AutoWeaponModule())
            add(AutoSoupModule())
            add(BlockClutchModule())
            add(AdvanceCombatAuraModule())

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
            add(BlinkModule())
            add(ClickTPModule())
            add(DashModule())
            add(GlideModule())
            add(IceSpeedModule())
            add(InvWalkModule())
            add(LongJumpModule())
            add(StepModule())
            add(WallClimbModule())
            add(AutoSprintModule())

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
            add(AmbienceModule())
            add(AnimationsModule())
            add(ChamsModule())
            add(ChunkBordersModule())
            add(ClearWaterModule())
            add(FOVSliderModule())
            add(ItemESPModule())
            add(NameTagsModule())
            add(NoWeatherModule())
            add(TracersModule())

            // World
            add(FreeCameraModule())
            add(TimeShiftModule())
            add(WeatherControllerModule())
            add(EffectsModule())
            add(ParticlesModule())
            add(AntiDebuffModule())
            add(AutoFarmModule())
            add(AutoFishModule())
            add(AutoMineModule())
            add(ChestESPModule())
            add(FastBreakModule())
            add(NukerModule())
            add(ScaffoldModule())
            add(XRayModule())
            add(AutoBuilderModule())
            add(PortalClickerModule())

            // Misc
            add(AutoDisconnectModule())
            add(ArrayListModule())
            add(ToggleSoundModule())
            add(ChestStealerModule())
            add(DesyncModule())
            add(SpammerModule())
            add(WaterMarkModule())
            add(PositionLoggerModule())
            add(NoChatModule())
            add(CommandHandlerModule())
            add(ReplayModule())
            add(PieChartModule())
            add(FakeDeathModule())
            add(FakeXPModule())
            add(MinerModule())
            add(AutoToolModule())
            add(DerpModule())
            add(FastPlaceModule())
            add(InventoryCleanerModule())
            add(MCFModule())
            add(NoFallModule())
            add(NoRotateModule())
            add(PingSpoofModule())
            add(TimerModule())
        }
    }

    fun saveConfig() {

        if (!AppContext.isInitialized) {
            return
        }

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

        config.writeText(json.encodeToString(JsonObject.serializer(), jsonObject))
    }

    fun loadConfig() {

        if (!AppContext.isInitialized) {
            return
        }

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

        try {
            val jsonObject = json.parseToJsonElement(jsonString).jsonObject
            val modules = jsonObject["modules"]?.jsonObject ?: return

            _modules.forEach { module ->
                (modules[module.name] as? JsonObject)?.let {
                    module.fromJson(it)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
        return json.encodeToString(JsonObject.serializer(), jsonObject)
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
            throw IllegalArgumentException("Invalid config format: ${e.message}")
        }
    }

    fun exportConfigToFile(context: Context, filePath: String): Boolean {
        return try {
            val file = if (filePath.contains("/")) {
                File(filePath)
            } else {
                val configsDir = context.getExternalFilesDir("configs")
                configsDir?.mkdirs()
                File(configsDir, if (filePath.endsWith(".json")) filePath else "$filePath.json")
            }

            file.parentFile?.mkdirs()
            file.writeText(exportConfig())
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getWClientConfigsDirectory(): File? {
        return try {
            val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

            val baseDir = if (documentsDir.exists() || documentsDir.mkdirs()) {
                documentsDir
            } else {
                downloadsDir
            }

            val wclientDir = File(baseDir, "WClient")
            val configsDir = File(wclientDir, "configs")

            if (configsDir.exists() || configsDir.mkdirs()) {
                configsDir
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
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