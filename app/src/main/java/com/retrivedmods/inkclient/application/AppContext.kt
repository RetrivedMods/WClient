package com.retrivedmods.inkclient.application

import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.Process
import com.retrivedmods.inkclient.activity.CrashHandlerActivity

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
}
