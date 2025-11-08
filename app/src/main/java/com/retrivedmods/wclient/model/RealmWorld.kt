package com.retrivedmods.wclient.model

import androidx.compose.runtime.Immutable
import net.raphimc.minecraftauth.service.realms.model.RealmsWorld

@Immutable
data class RealmWorld(
    val id: Long,
    val ownerName: String,
    val ownerUuidOrXuid: String,
    val name: String,
    val motd: String,
    val state: RealmState,
    val expired: Boolean,
    val worldType: String,
    val maxPlayers: Int,
    val compatible: Boolean,
    val activeVersion: String,
    val connectionDetails: RealmConnectionDetails? = null
) {
    companion object {
        fun fromRealmsWorld(realmsWorld: RealmsWorld): RealmWorld {
            return RealmWorld(
                id = realmsWorld.id,
                ownerName = realmsWorld.ownerName ?: "Unknown",
                ownerUuidOrXuid = realmsWorld.ownerUuidOrXuid ?: "",
                name = realmsWorld.name ?: "Unnamed Realm",
                motd = realmsWorld.motd ?: "",
                state = RealmState.fromString(realmsWorld.state),
                expired = realmsWorld.isExpired,
                worldType = realmsWorld.worldType ?: "NORMAL",
                maxPlayers = realmsWorld.maxPlayers,
                compatible = realmsWorld.isCompatible,
                activeVersion = realmsWorld.activeVersion ?: ""
            )
        }
    }
}

@Immutable
data class RealmConnectionDetails(
    val address: String,
    val port: Int,
    val fetchedAt: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    fun isExpired(): Boolean {
        val cacheExpirationTime = 2 * 60 * 60 * 1000L
        return System.currentTimeMillis() - fetchedAt > cacheExpirationTime
    }

    fun withError(errorMessage: String): RealmConnectionDetails {
        return copy(isLoading = false, error = errorMessage)
    }

    companion object {
        fun fromAddress(address: String): RealmConnectionDetails {
            val cleanAddress = address.trim()

            if (cleanAddress.isBlank()) {
                throw IllegalArgumentException("Address cannot be empty")
            }

            val parts = cleanAddress.split(":")
            return if (parts.size == 2) {
                val host = parts[0].trim()
                val portStr = parts[1].trim()

                if (host.isBlank() || host.length < 3) {
                    throw IllegalArgumentException("Invalid host: $host")
                }

                val port = portStr.toIntOrNull()
                if (port == null || port <= 0 || port > 65535) {
                    throw IllegalArgumentException("Invalid port: $portStr")
                }

                RealmConnectionDetails(
                    address = host,
                    port = port
                )
            } else if (parts.size == 1) {
                val host = parts[0].trim()
                if (host.isBlank() || host.length < 3) {
                    throw IllegalArgumentException("Invalid host: $host")
                }
                RealmConnectionDetails(
                    address = host,
                    port = 19132
                )
            } else {
                throw IllegalArgumentException("Invalid address format: $cleanAddress")
            }
        }

        fun loading(): RealmConnectionDetails {
            return RealmConnectionDetails(
                address = "",
                port = 19132,
                isLoading = true
            )
        }
    }
}

enum class RealmState {
    OPEN,
    CLOSED,
    UNKNOWN;

    companion object {
        fun fromString(state: String?): RealmState {
            return when (state?.uppercase()) {
                "OPEN" -> OPEN
                "CLOSED" -> CLOSED
                else -> UNKNOWN
            }
        }
    }
}

sealed class RealmsLoadingState {
    object Loading : RealmsLoadingState()
    data class Success(val realms: List<RealmWorld>) : RealmsLoadingState()
    data class Error(val message: String) : RealmsLoadingState()
    object NotAvailable : RealmsLoadingState()
    object NoAccount : RealmsLoadingState()
}