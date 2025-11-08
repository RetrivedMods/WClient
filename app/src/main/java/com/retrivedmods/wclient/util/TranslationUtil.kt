package com.retrivedmods.wclient.util

inline val String.translatedSelf: String
    get() = this.toDisplayName()

fun String.toDisplayName(): String {
    if (this.isEmpty()) return this

    val specialCases = mapOf(
        "cps" to "CPS",
        "fps" to "FPS",
        "esp" to "ESP",
        "tp" to "TP",
        "xp" to "XP",
        "2d" to "2D",
        "3d" to "3D",
        "fov" to "FOV",
        "hud" to "HUD",
        "gui" to "GUI",
        "afk" to "AFK",
        "bhop" to "BHop",
        "pps" to "PPS",
        "rgb" to "RGB",
        "bg" to "BG",
        "npc" to "NPC",
        "api" to "API",
        "ui" to "UI",
        "id" to "ID",
        "uuid" to "UUID",
        "url" to "URL",
        "ip" to "IP",
        "ms" to "MS",
        "kb" to "KB",
        "mb" to "MB",
        "gb" to "GB"
    )

    val words = if (this.contains('_')) {
        this.split('_')
    } else {
        this.replace(Regex("([a-z])([A-Z])"), "$1 $2").split(' ')
    }
    
    return words.joinToString(" ") { word ->
        val lowerWord = word.lowercase()

        specialCases[lowerWord] ?: run {
            if (word.any { it.isDigit() }) {
                word.uppercase()
            } else {
                word.replaceFirstChar { it.uppercase() }
            }
        }
    }
}