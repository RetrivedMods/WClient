package com.retrivedmods.wclient.model

import android.content.SharedPreferences
import androidx.core.content.edit
import com.retrivedmods.wclient.util.ServerCompatUtils

data class CaptureModeModel(
    val serverHostName: String,
    val serverPort: Int,
    val serverConfigType: ServerCompatUtils.ServerConfigType = ServerCompatUtils.ServerConfigType.STANDARD,
    val enableServerOptimizations: Boolean = true
) {

    companion object {

        fun from(sharedPreferences: SharedPreferences): CaptureModeModel {
            val serverHostName = sharedPreferences.getString(
                "capture_mode_model_server_host_name",
                "play.lbsg.net"
            )!!
            val serverPort = sharedPreferences.getInt(
                "capture_mode_model_server_port",
                19132
            )
            val serverConfigTypeName = sharedPreferences.getString(
                "capture_mode_model_server_config_type",
                ServerCompatUtils.ServerConfigType.STANDARD.name
            )!!
            val serverConfigType = try {
                ServerCompatUtils.ServerConfigType.valueOf(serverConfigTypeName)
            } catch (e: IllegalArgumentException) {
                ServerCompatUtils.ServerConfigType.STANDARD
            }
            val enableServerOptimizations = sharedPreferences.getBoolean(
                "capture_mode_model_enable_server_optimizations",
                true
            )

            return CaptureModeModel(
                serverHostName = serverHostName,
                serverPort = serverPort,
                serverConfigType = serverConfigType,
                enableServerOptimizations = enableServerOptimizations
            )
        }

    }

    fun to(sharedPreferences: SharedPreferences) {
        sharedPreferences.edit {
            putString(
                "capture_mode_model_server_host_name",
                serverHostName
            )
            putInt(
                "capture_mode_model_server_port",
                serverPort
            )
            putString(
                "capture_mode_model_server_config_type",
                serverConfigType.name
            )
            putBoolean(
                "capture_mode_model_enable_server_optimizations",
                enableServerOptimizations
            )
        }
    }

    /**
     * Auto-detect and update server configuration based on hostname
     */
    fun withAutoDetectedServerConfig(): CaptureModeModel {
        val isProtected = ServerCompatUtils.isProtectedServer(serverHostName)
        return if (isProtected) {
            val recommendedConfig = ServerCompatUtils.getRecommendedConfigType(serverHostName)
            copy(
                serverConfigType = recommendedConfig,
                enableServerOptimizations = true
            )
        } else {
            copy(
                serverConfigType = ServerCompatUtils.ServerConfigType.STANDARD,
                enableServerOptimizations = false
            )
        }
    }

    /**
     * Check if this configuration is for a protected server
     */
    fun isProtectedServer(): Boolean {
        return ServerCompatUtils.isProtectedServer(serverHostName)
    }

}