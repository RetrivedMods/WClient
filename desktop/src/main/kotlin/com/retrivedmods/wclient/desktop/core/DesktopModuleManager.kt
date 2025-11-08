package com.retrivedmods.wclient.desktop.core

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

object DesktopModuleManager {
    
    private val modules = mutableListOf<DesktopModule>()
    private val configFile = File(System.getProperty("user.home"), ".novaclient/config.json")
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    init {
        configFile.parentFile?.mkdirs()
        initializeModules()
    }
    
    private fun initializeModules() {
        modules.clear()
        
        modules.add(DesktopModule("killaura", "Combat", false))
        modules.add(DesktopModule("hitbox", "Combat", false))
        modules.add(DesktopModule("antiknockback", "Combat", false))
        modules.add(DesktopModule("triggerbot", "Combat", false))
        modules.add(DesktopModule("crystalsmash", "Combat", false))
        modules.add(DesktopModule("anticrystal", "Combat", false))
        modules.add(DesktopModule("hitandrun", "Combat", false))
        
        modules.add(DesktopModule("fly", "Motion", false))
        modules.add(DesktopModule("speed", "Motion", false))
        modules.add(DesktopModule("bhop", "Motion", false))
        modules.add(DesktopModule("highjump", "Motion", false))
        modules.add(DesktopModule("airjump", "Motion", false))
        modules.add(DesktopModule("jetpack", "Motion", false))
        modules.add(DesktopModule("spider", "Motion", false))
        modules.add(DesktopModule("sprint", "Motion", false))
        modules.add(DesktopModule("autowalk", "Motion", false))
        modules.add(DesktopModule("antiafk", "Motion", false))
        modules.add(DesktopModule("noclip", "Motion", false))
        modules.add(DesktopModule("unifiedfly", "Motion", false))
        modules.add(DesktopModule("motionfly", "Motion", false))
        
        modules.add(DesktopModule("esp", "Visual", false))
        modules.add(DesktopModule("zoom", "Visual", false))
        modules.add(DesktopModule("freecamera", "Visual", false))
        modules.add(DesktopModule("nohurtcamera", "Visual", false))
        modules.add(DesktopModule("timeshift", "Visual", false))
        modules.add(DesktopModule("weathercontroller", "Visual", false))
        modules.add(DesktopModule("minimap", "Visual", false))
        modules.add(DesktopModule("crosshair", "Visual", false))
        modules.add(DesktopModule("targethud", "Visual", false))
        
        modules.add(DesktopModule("nightvision", "Effect", false))
        modules.add(DesktopModule("haste", "Effect", false))
        modules.add(DesktopModule("speed", "Effect", false))
        modules.add(DesktopModule("jumpboost", "Effect", false))
        modules.add(DesktopModule("strength", "Effect", false))
        modules.add(DesktopModule("regeneration", "Effect", false))
        modules.add(DesktopModule("resistance", "Effect", false))
        modules.add(DesktopModule("fireresistance", "Effect", false))
        modules.add(DesktopModule("waterbreathing", "Effect", false))
        modules.add(DesktopModule("invisibility", "Effect", false))
        
        modules.add(DesktopModule("desync", "Misc", false))
        modules.add(DesktopModule("fakedeath", "Misc", false))
        modules.add(DesktopModule("nochat", "Misc", false))
        modules.add(DesktopModule("positionlogger", "Misc", false))
        modules.add(DesktopModule("replay", "Misc", false))
        modules.add(DesktopModule("baritone", "Misc", false))
        modules.add(DesktopModule("cheststealer", "Misc", false))
    }
    
    fun getModules(): List<DesktopModule> {
        return modules.toList()
    }
    
    fun toggleModule(name: String) {
        val module = modules.find { it.name.equals(name, ignoreCase = true) }
        module?.enabled = !(module?.enabled ?: false)
    }
    
    fun isModuleEnabled(name: String): Boolean {
        return modules.find { it.name.equals(name, ignoreCase = true) }?.enabled ?: false
    }
    
    fun saveConfig() {
        try {
            val config = ModuleConfig(modules.map { ModuleState(it.name, it.enabled) })
            val jsonString = json.encodeToString(ModuleConfig.serializer(), config)
            configFile.writeText(jsonString)
        } catch (e: Exception) {
            println("Failed to save config: ${e.message}")
        }
    }
    
    fun loadConfig() {
        try {
            if (!configFile.exists()) return
            
            val jsonString = configFile.readText()
            val config = json.decodeFromString(ModuleConfig.serializer(), jsonString)
            
            config.modules.forEach { state ->
                val module = modules.find { it.name == state.name }
                module?.enabled = state.enabled
            }
        } catch (e: Exception) {
            println("Failed to load config: ${e.message}")
        }
    }
    
    @Serializable
    data class ModuleConfig(val modules: List<ModuleState>)
    
    @Serializable
    data class ModuleState(val name: String, val enabled: Boolean)
}

data class DesktopModule(
    val name: String,
    val category: String,
    var enabled: Boolean
)
