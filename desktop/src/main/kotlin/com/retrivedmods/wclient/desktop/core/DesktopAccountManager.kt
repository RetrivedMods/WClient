package com.retrivedmods.wclient.desktop.core

import com.google.gson.JsonParser
import com.radiantbyte.novarelay.util.AuthUtils
import net.lenni0451.commons.httpclient.RetryHandler
import net.raphimc.minecraftauth.MinecraftAuth
import net.raphimc.minecraftauth.step.bedrock.session.StepFullBedrockSession
import net.raphimc.minecraftauth.step.msa.StepMsaDeviceCode
import java.io.File

object DesktopAccountManager {
    
    private val accounts = mutableListOf<StepFullBedrockSession.FullBedrockSession>()
    private var selectedAccount: StepFullBedrockSession.FullBedrockSession? = null
    
    private val accountsDir = File(System.getProperty("user.home"), ".novaclient/accounts")
    
    init {
        accountsDir.mkdirs()
    }
    
    fun loadAccounts() {
        accounts.clear()
        
        accountsDir.listFiles()?.forEach { file ->
            if (file.extension == "json" && file.name != "selected.txt") {
                try {
                    val json = JsonParser.parseString(file.readText()).asJsonObject
                    val account = MinecraftAuth.BEDROCK_DEVICE_CODE_LOGIN.fromJson(json)
                    accounts.add(account)
                } catch (e: Exception) {
                    println("Failed to load account from ${file.name}: ${e.message}")
                }
            }
        }
        
        val selectedFile = File(accountsDir, "selected.txt")
        if (selectedFile.exists()) {
            val selectedName = selectedFile.readText()
            selectedAccount = accounts.find { it.mcChain.displayName == selectedName }
        }
    }
    
    fun authenticateNewAccount(callback: (String) -> Unit): StepFullBedrockSession.FullBedrockSession {
        val httpClient = MinecraftAuth.createHttpClient()
        httpClient.connectTimeout = 30000
        httpClient.readTimeout = 30000
        httpClient.setRetryHandler(RetryHandler(3, Int.MAX_VALUE))
        
        val session = MinecraftAuth.BEDROCK_DEVICE_CODE_LOGIN.getFromInput(
            httpClient,
            StepMsaDeviceCode.MsaDeviceCodeCallback { deviceCode ->
                callback(deviceCode.directVerificationUri)
            }
        )
        
        val file = File(accountsDir, "${session.mcChain.displayName}.json")
        val json = AuthUtils.gson.toJson(MinecraftAuth.BEDROCK_DEVICE_CODE_LOGIN.toJson(session))
        file.writeText(json)
        
        accounts.add(session)
        
        return session
    }
    
    fun getAccounts(): List<StepFullBedrockSession.FullBedrockSession> {
        return accounts.toList()
    }
    
    fun getSelectedAccount(): StepFullBedrockSession.FullBedrockSession? {
        return selectedAccount
    }
    
    fun selectAccount(displayName: String) {
        selectedAccount = accounts.find { it.mcChain.displayName == displayName }
        
        val selectedFile = File(accountsDir, "selected.txt")
        if (selectedAccount != null) {
            selectedFile.writeText(selectedAccount!!.mcChain.displayName)
        } else {
            selectedFile.delete()
        }
    }
    
    fun removeAccount(displayName: String) {
        val account = accounts.find { it.mcChain.displayName == displayName }
        if (account != null) {
            accounts.remove(account)
            File(accountsDir, "$displayName.json").delete()
            
            if (selectedAccount == account) {
                selectedAccount = null
                File(accountsDir, "selected.txt").delete()
            }
        }
    }
}
