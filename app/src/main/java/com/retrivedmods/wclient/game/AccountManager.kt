package com.retrivedmods.wclient.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.JsonParser
import com.retrivedmods.wclient.application.AppContext
import com.retrivedmods.wclient.service.RealmsManager
import com.retrivedmods.wrelay.util.AuthUtils
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.raphimc.minecraftauth.MinecraftAuth
import net.raphimc.minecraftauth.bedrock.BedrockAuthManager
import java.io.File
import java.util.concurrent.TimeUnit

object AccountManager {

    // Bedrock game version reported to Mojang/Xbox services when negotiating a Minecraft
    // session (net.raphimc.minecraftauth.bedrock.model.MinecraftSession). Keep this in sync with
    // com.retrivedmods.wclient.util.MinecraftUtils.RECOMMENDED_VERSION.
    const val GAME_VERSION = "1.21.132"

    // BedrockAuthManager (5.x) is a live, self-refreshing manager object rather than the old
    // immutable FullBedrockSession, so we pair it with a plain cached display name for UI use
    // (looking up the name via the manager would be a network call if the token had expired).
    data class WAccount(
        val authManager: BedrockAuthManager,
        val displayName: String
    )

    private val coroutineScope =
        CoroutineScope(Dispatchers.IO + CoroutineName("AccountManagerCoroutine"))

    private val _accounts: MutableList<WAccount> = mutableStateListOf()

    val accounts: List<WAccount>
        get() = _accounts

    var selectedAccount: WAccount? by mutableStateOf(null)
        private set

    private val TOKEN_REFRESH_INTERVAL_MS = TimeUnit.MINUTES.toMillis(30)

    init {
        val fetchedAccounts = fetchAccounts()

        _accounts.addAll(fetchedAccounts)
        selectedAccount = fetchSelectedAccount()

        RealmsManager.updateSession(selectedAccount)

        startTokenRefreshScheduler()
    }

    fun addAccount(authManager: BedrockAuthManager) {
        val displayName = authManager.minecraftCertificateChain.getUpToDate().identityDisplayName
        val account = WAccount(authManager, displayName)

        val existingAccount = _accounts.find { it.displayName == displayName }
        if (existingAccount != null) {
            _accounts.remove(existingAccount)
        }

        _accounts.add(account)

        if (existingAccount == selectedAccount) {
            selectAccount(account)
        }

        saveAccountToDisk(account)
    }

    fun removeAccount(account: WAccount) {
        _accounts.remove(account)

        coroutineScope.launch {
            val file = File(AppContext.instance.cacheDir, "accounts")
            file.mkdirs()

            file.resolve("${account.displayName}.json").delete()
        }
    }

    fun selectAccount(account: WAccount?) {
        selectedAccount = account

        RealmsManager.updateSession(account)

        coroutineScope.launch {
            val file = File(AppContext.instance.cacheDir, "accounts")
            file.mkdirs()

            runCatching {
                val selectedAccountFile = file.resolve("selectedAccount")
                if (account != null) {
                    selectedAccountFile.writeText(account.displayName)
                } else {
                    selectedAccountFile.delete()
                }
            }
        }
    }

    private fun fetchAccounts(): List<WAccount> {
        val file = File(AppContext.instance.cacheDir, "accounts")
        file.mkdirs()

        val accounts = ArrayList<WAccount>()
        val listFiles = file.listFiles() ?: emptyArray()
        for (child in listFiles) {
            runCatching {
                if (child.isFile && child.extension == "json") {
                    val httpClient = MinecraftAuth.createHttpClient()
                    val json = JsonParser.parseString(child.readText()).asJsonObject
                    val authManager = BedrockAuthManager.fromJson(httpClient, GAME_VERSION, json)
                    val displayName =
                        authManager.minecraftCertificateChain.getUpToDate().identityDisplayName
                    accounts.add(WAccount(authManager, displayName))
                    println("Loaded account $displayName")
                }
            }.onFailure {
                println("Failed to load account from ${child.name}: ${it.message}")
                it.printStackTrace()
            }
        }

        return accounts
    }

    private fun fetchSelectedAccount(): WAccount? {
        val file = File(AppContext.instance.cacheDir, "accounts")
        file.mkdirs()

        val selectedAccountFile = file.resolve("selectedAccount")
        if (!selectedAccountFile.exists() || selectedAccountFile.isDirectory) {
            return null
        }

        val displayName = selectedAccountFile.readText()
        return accounts.find { it.displayName == displayName }
    }

    private fun startTokenRefreshScheduler() {
        coroutineScope.launch {
            while (true) {
                try {
                    refreshExpiredTokens()
                } catch (e: Exception) {
                    println("Error during token refresh: ${e.message}")
                    e.printStackTrace()
                }

                delay(TOKEN_REFRESH_INTERVAL_MS)
            }
        }
    }

    private fun refreshExpiredTokens() {
        if (_accounts.isEmpty()) {
            return
        }

        _accounts.forEach { account ->
            try {
                // getUpToDate() only performs a network refresh when the cached value is
                // missing/expired, so this is a no-op for accounts that are already fresh -
                // no more manually tracking expiry thresholds or swapping session objects.
                account.authManager.minecraftCertificateChain.getUpToDate()
                account.authManager.playFabToken.getUpToDate()
                saveAccountToDisk(account)
            } catch (e: Exception) {
                println("Failed to refresh token for ${account.displayName}: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun saveAccountToDisk(account: WAccount) {
        coroutineScope.launch {
            val file = File(AppContext.instance.cacheDir, "accounts")
            file.mkdirs()

            try {
                val json = BedrockAuthManager.toJson(account.authManager)
                file.resolve("${account.displayName}.json")
                    .writeText(AuthUtils.gson.toJson(json))
            } catch (e: Exception) {
                println("Failed to save account ${account.displayName}: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}
