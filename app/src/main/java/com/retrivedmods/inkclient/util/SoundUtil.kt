package com.retrivedmods.inkclient.util

import android.media.AudioAttributes
import android.media.SoundPool
import com.retrivedmods.inkclient.R
import android.content.Context


object SoundUtil {

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private var enableSound = 0
    private var disableSound = 0
    private var loadedCount = 0

    fun load(context: Context) {
        soundPool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) {
                loadedCount++
            }
        }

        enableSound = soundPool.load(context, R.raw.on, 1)
        disableSound = soundPool.load(context, R.raw.off, 1)
    }

    fun playEnable() {
        if (loadedCount >= 2) {
            soundPool.play(enableSound, 1f, 1f, 1, 0, 1f)
        }
    }

    fun playDisable() {
        if (loadedCount >= 2) {
            soundPool.play(disableSound, 1f, 1f, 1, 0, 1f)
        }
    }
}
