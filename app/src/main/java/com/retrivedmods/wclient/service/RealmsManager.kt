package com.retrivedmods.wclient.service

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.raphimc.minecraftauth.MinecraftAuth
import net.raphimc.minecraftauth.service.realms.BedrockRealmsService
import net.raphimc.minecraftauth.step.bedrock.session.StepFullBedrockSession
import com.retrivedmods.wclient.model.RealmWorld
import com.retrivedmods.wclient.model.RealmConnectionDetails
import com.retrivedmods.wclient.model.RealmState
import com.retrivedmods.wclient.model.RealmsLoadingState
import java.util.concurrent.ConcurrentHashMap

object RealmsManager {

    private const val TAG = "RealmsManager"
    private const val CLIENT_VERSION = "1.21.120"

    private val coroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("RealmsManagerCoroutine"))

    private val _realmsState = MutableStateFlow<RealmsLoadingState>(RealmsLoadingState.NoAccount)
    val realmsState: StateFlow<RealmsLoadingState> = _realmsState.asStateFlow()

    private val connectionCache = ConcurrentHashMap<Long, RealmConnectionDetails>()

    private var currentSession: StepFullBedrockSession.FullBedrockSession? = null
    private var realmsService: BedrockRealmsService? = null

    fun updateSession(session: StepFullBedrockSession.FullBedrockSession?) {
        currentSession = session

        Log.d(TAG, "updateSession called with session: ${session?.mcChain?.displayName}")
        Log.d(TAG, "Session has realmsXsts: ${session?.realmsXsts != null}")

        if (session?.realmsXsts != null) {
            try {
                Log.d(TAG, "Initializing Realms service with client version: $CLIENT_VERSION")
                val httpClient = MinecraftAuth.createHttpClient()
                httpClient.connectTimeout = 10000
                httpClient.readTimeout = 10000

                realmsService = BedrockRealmsService(httpClient, CLIENT_VERSION, session.realmsXsts)
                Log.d(TAG, "Realms service initialized successfully")
                refreshRealms()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize Realms service", e)
                _realmsState.value = RealmsLoadingState.Error("Failed to initialize Realms service: ${e.message}")
            }
        } else {
            Log.w(TAG, "No realmsXsts token available - session: ${session != null}, realmsXsts: ${session?.realmsXsts}")
            realmsService = null
            _realmsState.value = if (session == null) {
                RealmsLoadingState.NoAccount
            } else {
                RealmsLoadingState.NotAvailable
            }
        }
    }

    fun refreshRealms() {
        val service = realmsService
        if (service == null) {
            Log.w(TAG, "refreshRealms called but realmsService is null")
            _realmsState.value = RealmsLoadingState.NoAccount
            return
        }

        Log.d(TAG, "Starting Realms refresh with client version: $CLIENT_VERSION")
        _realmsState.value = RealmsLoadingState.Loading

        coroutineScope.launch {
            try {
                Log.d(TAG, "Checking if Realms is available...")
                val isAvailable = service.isAvailable().get()
                Log.d(TAG, "Realms availability check result: $isAvailable")

                if (!isAvailable) {
                    Log.w(TAG, "Realms not available for client version: $CLIENT_VERSION")
                    _realmsState.value = RealmsLoadingState.NotAvailable
                    return@launch
                }

                Log.d(TAG, "Fetching Realms worlds...")
                val realmsWorlds = service.worlds.get()
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

                val realmsWorld = net.raphimc.minecraftauth.service.realms.model.RealmsWorld(
                    realm.id,
                    realm.ownerName,
                    realm.ownerUuidOrXuid,
                    realm.name,
                    realm.motd,
                    realm.state.name,
                    realm.expired,
                    realm.worldType,
                    realm.maxPlayers,
                    realm.compatible,
                    realm.activeVersion,
                    null
                )

                Log.d(TAG, "Requesting connection details for Realm ${realm.name} (ID: $realmId)")
                val address = withContext(Dispatchers.IO) {
                    service.joinWorld(realmsWorld).get()
                }

                Log.d(TAG, "Received raw address from Realms service: '$address'")

                if (address.isBlank()) {
                    throw IllegalStateException("Received empty address from Realms service")
                }

                val connectionDetails = try {
                    RealmConnectionDetails.fromAddress(address)
                } catch (e: IllegalArgumentException) {
                    Log.e(TAG, "Failed to parse address '$address': ${e.message}")
                    throw IllegalStateException("Invalid address format received: $address")
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