package com.retrivedmods.wclient.application

import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.Process
import com.retrivedmods.wclient.activity.CrashHandlerActivity

class AppContext : Application(), Thread.UncaughtExceptionHandler {

    companion object {
        lateinit var instance: AppContext
            private set

        // Helper to check if initialized
        val isInitialized: Boolean
            get() = ::instance.isInitialized
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        // Suppress known Android 16 Samsung ripple bug where a RippleDrawable
        // tries to start an animator on a detached view during the draw pass.
        // This is a framework-level race condition and does not corrupt app state.
        if (e is IllegalStateException && isRippleOnDetachedView(e)) {
            android.util.Log.w("WClient", "Suppressed ripple-on-detached-view crash", e)
            return
        }

        val stackTrace = e.stackTraceToString()
        val deviceInfo = buildString {
            val declaredFields = Build::class.java.declaredFields
            for (field in declaredFields) {
                field.isAccessible = true
                try {
                    val name = field.name
                    var value = field.get(null)

                    if (value == null) {
                        value = "null"
                    } else if (value.javaClass.isArray) {
                        value = (value as Array<out Any?>).contentDeepToString()
                    }

                    append(name)
                    append(": ")
                    appendLine(value)
                } catch (_: Throwable) {
                }
            }
        }

        startActivity(Intent(this, CrashHandlerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("message", buildString {
                appendLine("An unexpected exception / error happened!")
                appendLine("Please tell the developer to fix it!")
                appendLine()
                appendLine(deviceInfo)
                appendLine("Thread: ${t.name}")
                appendLine("Thread Group: ${t.threadGroup?.name}")
                appendLine()
                appendLine("Stack Trace: $stackTrace")
            })
        })
        Process.killProcess(Process.myPid())
    }

    private fun isRippleOnDetachedView(e: IllegalStateException): Boolean {
        if (e.message != "Cannot start this animator on a detached view!") return false
        // Confirm the crash originates from the RippleDrawable/RippleForeground code path
        val trace = e.stackTraceToString()
        return trace.contains("RippleForeground") ||
            trace.contains("RippleDrawable") ||
            trace.contains("RippleHostView") ||
            trace.contains("RippleNode")
    }
}