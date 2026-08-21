package com.retrivedmods.wclient.service

import android.util.Log
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.raphimc.minecraftauth.MinecraftAuth
import net.raphimc.minecraftauth.extra.realms.model.RealmsServer
import net.raphimc.minecraftauth.extra.realms.service.impl.BedrockRealmsService
import com.retrivedmods.wclient.game.AccountManager
import com.retrivedmods.wclient.model.RealmWorld
import com.retrivedmods.wclient.model.RealmConnectionDetails
import com.retrivedmods.wclient.model.RealmState
import com.retrivedmods.wclient.model.RealmsLoadingState
import java.util.concurrent.ConcurrentHashMap

object RealmsManager {

    private const val TAG = "RealmsManager"

    private val coroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("RealmsManagerCoroutine"))

    private val _realmsState = MutableStateFlow<RealmsLoadingState>(RealmsLoadingState.NoAccount)
    val realmsState: StateFlow<RealmsLoadingState> = _realmsState.asStateFlow()

    private val connectionCache = ConcurrentHashMap<Long, RealmConnectionDetails>()

    private var realmsService: BedrockRealmsService? = null

    fun updateSession(account: AccountManager.WAccount?) {
        Log.d(TAG, "updateSession called with account: ${account?.displayName}")

        if (account == null) {
            realmsService = null
            _realmsState.value = RealmsLoadingState.NoAccount
            return
        }

        try {
            Log.d(TAG, "Initializing Realms service with client version: ${AccountManager.GAME_VERSION}")
            val httpClient = MinecraftAuth.createHttpClient()

            // realmsXstsToken is a Holder<XblXstsToken> on BedrockAuthManager - it's fetched
            // lazily (on first getUpToDate() call from inside BedrockRealmsService), not eagerly
            // during sign-in, so accounts without Realms access don't fail login over this.
            realmsService = BedrockRealmsService(
                httpClient,
                AccountManager.GAME_VERSION,
                account.authManager.realmsXstsToken
            )
            Log.d(TAG, "Realms service initialized successfully")
            refreshRealms()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Realms service", e)
            _realmsState.value = RealmsLoadingState.Error("Failed to initialize Realms service: ${e.message}")
        }
    }

    fun refreshRealms() {
        val service = realmsService
        if (service == null) {
            Log.w(TAG, "refreshRealms called but realmsService is null")
            _realmsState.value = RealmsLoadingState.NoAccount
            return
        }

        Log.d(TAG, "Starting Realms refresh")
        _realmsState.value = RealmsLoadingState.Loading

        coroutineScope.launch {
            try {
                Log.d(TAG, "Checking Realms compatibility...")
                val isCompatible = service.isCompatible()
                Log.d(TAG, "Realms compatibility check result: $isCompatible")

                if (!isCompatible) {
                    Log.w(TAG, "Realms not available for this client version")
                    _realmsState.value = RealmsLoadingState.NotAvailable
                    return@launch
                }

                Log.d(TAG, "Fetching Realms worlds...")
                val realmsWorlds = service.worlds
                val realmWorldList = realmsWorlds.map { RealmWorld.fromRealmsWorld(it) }

                _realmsState.value = RealmsLoadingState.Success(realmWorldList)

                Log.d(TAG, "Successfully fetched ${realmWorldList.size} Realms")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch Realms", e)
                val errorMessage = when {
                    e.message?.contains("401") == true || e.message?.contains("Unauthorized") == true ->
                        "Authentication failed. Please reconnect your Microsoft account."
                    e.message?.contains("403") == true || e.message?.contains("Forbidden") == true ->
                        "Access denied. You may not have Realms access."
                    e.message?.contains("timeout") == true || e.message?.contains("timed out") == true ->
                        "Connection timed out. Please check your internet connection."
                    e.message?.contains("network") == true || e.message?.contains("connection") == true ->
                        "Network error. Please check your internet connection."
                    else -> "Failed to fetch Realms: ${e.message ?: "Unknown error"}"
                }
                _realmsState.value = RealmsLoadingState.Error(errorMessage)
            }
        }
    }

    fun getRealmConnectionDetails(realmId: Long, callback: (RealmConnectionDetails) -> Unit) {
        val service = realmsService
        if (service == null) {
            callback(RealmConnectionDetails.loading().withError("Realms service not available"))
            return
        }

        val cached = connectionCache[realmId]
        if (cached != null && !cached.isExpired() && cached.error == null) {
            callback(cached)
            return
        }

        val loadingDetails = RealmConnectionDetails.loading()
        connectionCache[realmId] = loadingDetails
        callback(loadingDetails)

        coroutineScope.launch {
            try {
                val currentState = _realmsState.value
                if (currentState !is RealmsLoadingState.Success) {
                    throw IllegalStateException("Realms not loaded")
                }

                val realm = currentState.realms.find { it.id == realmId }
                    ?: throw IllegalArgumentException("Realm not found")

                if (realm.expired) {
                    throw IllegalStateException("Realm has expired")
                }

                if (realm.state != RealmState.OPEN) {
                    throw IllegalStateException("Realm is not open (current state: ${realm.state})")
                }

                val realmsServer = RealmsServer(
                    realm.id,
                    realm.name,
                    realm.motd,
                    realm.ownerName,
                    realm.ownerUuidOrXuid,
                    realm.state.name,
                    realm.expired,
                    0,
                    realm.worldType,
                    realm.maxPlayers,
                    realm.compatible,
                    realm.activeVersion,
                    JsonObject()
                )

                Log.d(TAG, "Requesting connection details for Realm ${realm.name} (ID: $realmId)")
                val joinInfo = withContext(Dispatchers.IO) {
                    service.joinWorld(realmsServer)
                }

                Log.d(TAG, "Received raw address from Realms service: '${joinInfo.address}'")

                if (joinInfo.address.isBlank()) {
                    throw IllegalStateException("Received empty address from Realms service")
                }

                val connectionDetails = try {
                    RealmConnectionDetails.fromAddress(joinInfo.address)
                } catch (e: IllegalArgumentException) {
                    Log.e(TAG, "Failed to parse address '${joinInfo.address}': ${e.message}")
                    throw IllegalStateException("Invalid address format received: ${joinInfo.address}")
                }

                connectionCache[realmId] = connectionDetails
                callback(connectionDetails)

                Log.d(TAG, "Successfully got connection details for Realm $realmId: ${connectionDetails.address}:${connectionDetails.port}")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to get connection details for Realm $realmId", e)
                val errorMessage = when {
                    e.message?.contains("401") == true || e.message?.contains("Unauthorized") == true ->
                        "Authentication failed"
                    e.message?.contains("403") == true || e.message?.contains("Forbidden") == true ->
                        "Access denied to this Realm"
                    e.message?.contains("404") == true || e.message?.contains("Not Found") == true ->
                        "Realm not found or no longer available"
                    e.message?.contains("timeout") == true || e.message?.contains("timed out") == true ->
                        "Connection timed out"
                    e.message?.contains("network") == true || e.message?.contains("connection") == true ->
                        "Network error"
                    e is IllegalStateException -> e.message ?: "Realms not loaded"
                    e is IllegalArgumentException -> e.message ?: "Invalid realm"
                    else -> "Connection failed: ${e.message ?: "Unknown error"}"
                }
                val errorDetails = RealmConnectionDetails.loading().withError(errorMessage)
                connectionCache[realmId] = errorDetails
                callback(errorDetails)
            }
        }
    }
}
