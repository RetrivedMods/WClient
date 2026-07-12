package com.retrivedmods.inkclient.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.retrivedmods.inkclient.overlay.gui.classic.OverlayShortcutButton
import com.retrivedmods.inkclient.util.translatedSelf
import com.retrivedmods.inkclient.util.SoundUtil
import com.retrivedmods.inkclient.game.module.misc.ToggleSoundModule
import kotlinx.serialization.json.*

abstract class Module(
    val name: String,
    val category: ModuleCategory,
    defaultEnabled: Boolean = false,
    val private: Boolean = false
) : InterruptiblePacketHandler,
    AutoConfiguration {

    open lateinit var session: GameSession

    private var _isEnabled by mutableStateOf(defaultEnabled)

    var isEnabled: Boolean
        get() = _isEnabled
        set(value) {
            _isEnabled = value
            if (value) onEnabled() else onDisabled()
        }

    val isSessionCreated: Boolean
        get() = ::session.isInitialized

    var isExpanded by mutableStateOf(false)
    var isShortcutDisplayed by mutableStateOf(false)

    var shortcutX = 0
    var shortcutY = 100

    val overlayShortcutButton by lazy { OverlayShortcutButton(this) }

    override val values: MutableList<Value<*>> = ArrayList()



    open fun onEnabled() {
        sendToggleMessage(true)

        if (shouldPlayToggleSound()) {
            SoundUtil.playEnable()
        }
    }

    open fun onDisabled() {
        sendToggleMessage(false)

        if (shouldPlayToggleSound()) {
            SoundUtil.playDisable()
        }
    }

    private fun shouldPlayToggleSound(): Boolean {
        if (this is ToggleSoundModule) return false

        val toggleSoundModule = ModuleManager.modules
            .firstOrNull { it is ToggleSoundModule } as? ToggleSoundModule

        return toggleSoundModule?.isEnabled == true
    }




    open fun toJson() = buildJsonObject {
        put("state", isEnabled)
        put("values", buildJsonObject {
            values.forEach { value ->
                put(value.name, value.toJson())
            }
        })
        if (isShortcutDisplayed) {
            put("shortcut", buildJsonObject {
                put("x", shortcutX)
                put("y", shortcutY)
            })
        }
    }

    open fun fromJson(jsonElement: JsonElement) {
        if (jsonElement is JsonObject) {
            isEnabled = (jsonElement["state"] as? JsonPrimitive)?.boolean ?: isEnabled

            (jsonElement["values"] as? JsonObject)?.forEach { (key, json) ->
                val value = getValue(key) ?: return@forEach
                try {
                    value.fromJson(json)
                } catch (_: Throwable) {
                    value.reset()
                }
            }

            (jsonElement["shortcut"] as? JsonObject)?.let {
                shortcutX = (it["x"] as? JsonPrimitive)?.int ?: shortcutX
                shortcutY = (it["y"] as? JsonPrimitive)?.int ?: shortcutY
                isShortcutDisplayed = true
            }
        }
    }




    private fun sendToggleMessage(enabled: Boolean) {
        if (!isSessionCreated) return

        val stateText = if (enabled) "enabled".translatedSelf else "disabled".translatedSelf
        val status = (if (enabled) "§a" else "§c") + stateText
        val moduleName = name.translatedSelf

        session.displayClientMessage(
            "§l§c[InkClient] §r§7$moduleName §8» $status"
        )
    }
}
