package com.retrivedmods.wclient.game.utils

object TimerUtil {

    private var currentMs: Float = getTime()


    fun getTime(): Float {
        return System.nanoTime() / 1_000_000f
    }



    fun reset() {
        currentMs = getTime()
    }

    fun hasTimeElapsed(ms: Float): Boolean {
        if (getTime() - currentMs >= ms) {
            reset()
            return true
        }
        return false
    }
}
