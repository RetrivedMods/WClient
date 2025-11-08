package com.retrivedmods.wclient.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.JsonParser
import com.retrivedmods.wclient.application.AppContext
import com.retrivedmods.wclient.game.RealmsAuthFlow
import com.retrivedmods.wclient.service.RealmsManager
import com.retrivedmods.wrelay.util.AuthUtils
import com.retrivedmods.wrelay.util.refresh
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.raphimc.minecraftauth.MinecraftAuth
import net.raphimc.minecraftauth.step.bedrock.session.StepFullBedrockSession.FullBedrockSession
import java.io.File
import java.util.concurrent.TimeUnit

object AccountManager {

    private val coroutineScope =
        CoroutineScope(Dispatchers.IO + CoroutineName("AccountManagerCoroutine"))

    private val _accounts: MutableList<FullBedrockSession> = mutableStateListOf()

    val accounts: List<FullBedrockSession>
        get() = _accounts

    var selectedAccount: FullBedrockSession? by mutableStateOf(null)
        private set

    private val TOKEN_REFRESH_INTERVAL_MS = TimeUnit.MINUTES.toMillis(30)

    private val TOKEN_REFRESH_THRESHOLD_MS = TimeUnit.HOURS.toMillis(2)

    init {
        val fetchedAccounts = fetchAccounts()

        _accounts.addAll(fetchedAccounts)
        selectedAccount = fetchSelectedAccount()

        RealmsManager.updateSession(selectedAccount)

        startTokenRefreshScheduler()
    }

    fun addAccount(fullBedrockSession: FullBedrockSession) {
        val existingAccount = _accounts.find { it.mcChain.displayName == fullBedrockSession.mcChain.displayName }
        if (existingAccount != null) {
            _accounts.remove(existingAccount)
        }

        _accounts.add(fullBedrockSession)

        coroutineScope.launch {
            val file = File(AppContext.instance.cacheDir, "accounts")
            file.mkdirs()

            try {
                val json = if (fullBedrockSession.realmsXsts != null) {
                    RealmsAuthFlow.BEDROCK_DEVICE_CODE_LOGIN_WITH_REALMS.toJson(fullBedrockSession)
                } else {
                    println("No Realms token available, saving with regular auth flow")
                    MinecraftAuth.BEDROCK_DEVICE_CODE_LOGIN.toJson(fullBedrockSession)
                }
                file.resolve("${fullBedrockSession.mcChain.displayName}.json")
                    .writeText(AuthUtils.gson.toJson(json))
                println("Successfully saved account: ${fullBedrockSession.mcChain.displayName} - Realms support: ${fullBedrockSession.realmsXsts != null}")
            } catch (e: Exception) {
                println("Failed to save account with Realms support, trying fallback: ${e.message}")
                try {
                    val json = MinecraftAuth.BEDROCK_DEVICE_CODE_LOGIN.toJson(fullBedrockSession)
                    file.resolve("${fullBedrockSession.mcChain.displayName}.json")
                        .writeText(AuthUtils.gson.toJson(json))
                    println("Successfully saved account with fallback method: ${fullBedrockSession.mcChain.displayName}")
                } catch (fallbackException: Exception) {
                    println("Failed to save account even with fallback: ${fallbackException.message}")
                    fallbackException.printStackTrace()
                }
            }
        }
    }

    fun removeAccount(fullBedrockSession: FullBedrockSession) {
        _accounts.remove(fullBedrockSession)

        coroutineScope.launch {
            val file = File(AppContext.instance.cacheDir, "accounts")
            file.mkdirs()

            file.resolve("${fullBedrockSession.mcChain.displayName}.json")
                .delete()
        }
    }

    fun selectAccount(fullBedrockSession: FullBedrockSession?) {
        selectedAccount = fullBedrockSession

        RealmsManager.updateSession(fullBedrockSession)

        coroutineScope.launch {
            val file = File(AppContext.instance.cacheDir, "accounts")
            file.mkdirs()

            runCatching {
                val selectedAccount = file.resolve("selectedAccount")
                if (fullBedrockSession != null) {
                    selectedAccount.writeText(fullBedrockSession.mcChain.displayName)
                } else {
                    selectedAccount.delete()
                }
            }
        }
    }

    private fun fetchAccounts(): List<FullBedrockSession> {
        val file = File(AppContext.instance.cacheDir, "accounts")
        file.mkdirs()

        val accounts = ArrayList<FullBedrockSession>()
        val listFiles = file.listFiles() ?: emptyArray()
        for (child in listFiles) {
            runCatching {
                if (child.isFile && child.extension == "json") {
                    val account = try {
                        RealmsAuthFlow.BEDROCK_DEVICE_CODE_LOGIN_WITH_REALMS
                            .fromJson(JsonParser.parseString(child.readText()).asJsonObject)
                    } catch (e: Exception) {
                        println("Failed to load account with Realms support from ${child.name}, trying legacy format: ${e.message}")
                        MinecraftAuth.BEDROCK_DEVICE_CODE_LOGIN
                            .fromJson(JsonParser.parseString(child.readText()).asJsonObject)
                    }
                    accounts.add(account)
                    println("Loaded account ${account.mcChain.displayName} - Realms support: ${account.realmsXsts != null}")
                }
            }.onFailure {
                println("Failed to load account from ${child.name}: ${it.message}")
            }
        }

        return accounts
    }

    private fun fetchSelectedAccount(): FullBedrockSession? {
        val file = File(AppContext.instance.cacheDir, "accounts")
        file.mkdirs()

        val selectedAccount = file.resolve("selectedAccount")
        if (!selectedAccount.exists() || selectedAccount.isDirectory) {
            return null
        }

        val displayName = selectedAccount.readText()
        return accounts.find { it.mcChain.displayName == displayName }
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

        val accountsToRefresh = _accounts.filter { account ->
            shouldRefreshToken(account)
        }

        if (accountsToRefresh.isNotEmpty()) {
            println("Found ${accountsToRefresh.size} accounts that need token refresh")
        }

        accountsToRefresh.forEach { account ->
            try {
                println("Refreshing token for account: ${account.mcChain.displayName}")
                val httpClient = MinecraftAuth.createHttpClient()
                httpClient.connectTimeout = 10000
                httpClient.readTimeout = 10000

                val refreshedAccount = try {
                    if (account.realmsXsts != null) {
                        RealmsAuthFlow.BEDROCK_DEVICE_CODE_LOGIN_WITH_REALMS.refresh(httpClient, account)
                    } else {
                        account.refresh()
                    }
                } catch (e: Exception) {
                    println("Failed to refresh with Realms support, trying regular refresh: ${e.message}")
                    account.refresh()
                }

                val index = _accounts.indexOf(account)
                if (index >= 0) {
                    _accounts[index] = refreshedAccount

                    if (selectedAccount == account) {
                        selectedAccount = refreshedAccount
                        RealmsManager.updateSession(refreshedAccount)
                    }

                    saveAccountToDisk(refreshedAccount)
                }

                println("Successfully refreshed token for: ${refreshedAccount.mcChain.displayName}")
            } catch (e: Exception) {
                println("Failed to refresh token for ${account.mcChain.displayName}: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun shouldRefreshToken(account: FullBedrockSession): Boolean {
        val currentTime = System.currentTimeMillis()

        val msaToken = account.mcChain.xblXsts.initialXblSession.msaToken
        if (msaToken.expireTimeMs - currentTime < TOKEN_REFRESH_THRESHOLD_MS) {
            return true
        }

        val xblExpireTime = account.mcChain.xblXsts.expireTimeMs
        if (xblExpireTime - currentTime < TOKEN_REFRESH_THRESHOLD_MS) {
            return true
        }

        val playFabExpireTime = account.playFabToken.expireTimeMs
        if (playFabExpireTime - currentTime < TOKEN_REFRESH_THRESHOLD_MS) {
            return true
        }

        return false
    }

    private fun saveAccountToDisk(account: FullBedrockSession) {
        val file = File(AppContext.instance.cacheDir, "accounts")
        file.mkdirs()

        try {
            val json = if (account.realmsXsts != null) {
                RealmsAuthFlow.BEDROCK_DEVICE_CODE_LOGIN_WITH_REALMS.toJson(account)
            } else {
                MinecraftAuth.BEDROCK_DEVICE_CODE_LOGIN.toJson(account)
            }
            file.resolve("${account.mcChain.displayName}.json")
                .writeText(AuthUtils.gson.toJson(json))
        } catch (e: Exception) {
            println("Failed to save account with Realms support, trying fallback: ${e.message}")
            try {
                val json = MinecraftAuth.BEDROCK_DEVICE_CODE_LOGIN.toJson(account)
                file.resolve("${account.mcChain.displayName}.json")
                    .writeText(AuthUtils.gson.toJson(json))
            } catch (fallbackException: Exception) {
                println("Failed to save account even with fallback: ${fallbackException.message}")
                fallbackException.printStackTrace()
            }
        }
    }
}