package com.retrivedmods.wclient.ui.component

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.retrivedmods.wclient.game.AccountManager
import com.retrivedmods.wclient.game.RealmsAuthFlow
import net.raphimc.minecraftauth.MinecraftAuth
import net.raphimc.minecraftauth.bedrock.BedrockAuthManager
import net.raphimc.minecraftauth.msa.service.impl.DeviceCodeMsaAuthService
import kotlin.concurrent.thread

val auth = "UCxb4pcHvdYpqv7i5Xt9mOUw"

val authId = "$auth"


@SuppressLint("SetJavaScriptEnabled")
class AuthWebView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : WebView(context, attrs) {

    var callback: ((Throwable?) -> Unit)? = null

    init {
        CookieManager.getInstance()
            .removeAllCookies(null)

        settings.javaScriptEnabled = true
        webViewClient = AuthWebViewClient()
    }

    fun addAccount() {
        thread {
            runCatching {
                val httpClient = MinecraftAuth.createHttpClient()

                // Unlike the old 4.x step-chain API, BedrockAuthManager fetches tokens lazily
                // and per-purpose (Realms XSTS is only requested later, if/when RealmsManager
                // actually needs it) - so there's no separate "Realms-capable chain that can
                // fail for accounts without Realms" to fall back from anymore.
                val authManager = BedrockAuthManager
                    .create(httpClient, AccountManager.GAME_VERSION)
                    .msaApplicationConfig(RealmsAuthFlow.BEDROCK_ANDROID_APPLICATION_CONFIG)
                    .login(::DeviceCodeMsaAuthService) { deviceCode ->
                        post {
                            loadUrl(deviceCode.directVerificationUri)
                        }
                    }

                AccountManager.addAccount(authManager)
                callback?.invoke(null)
            }.exceptionOrNull()?.let {
                callback?.invoke(it)
            }
        }
    }

    inner class AuthWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            return false
        }

    }

}
