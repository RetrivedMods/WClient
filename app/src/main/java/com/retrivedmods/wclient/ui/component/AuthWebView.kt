package com.retrivedmods.wclient.ui.component

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Base64
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.retrivedmods.wclient.game.AccountManager
import com.retrivedmods.wclient.game.RealmsAuthFlow
import net.raphimc.minecraftauth.MinecraftAuth
import net.raphimc.minecraftauth.step.msa.StepMsaDeviceCode
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
                httpClient.connectTimeout = 10000
                httpClient.readTimeout = 10000

                val fullBedrockSession = RealmsAuthFlow.BEDROCK_DEVICE_CODE_LOGIN_WITH_REALMS.getFromInput(
                    httpClient,
                    StepMsaDeviceCode.MsaDeviceCodeCallback {
                        post {
                            loadUrl(it.directVerificationUri)
                        }
                    }
                )
                val containedAccount =
                    AccountManager.accounts.find { it.mcChain.displayName == fullBedrockSession.mcChain.displayName }
                if (containedAccount != null) {
                    AccountManager.removeAccount(containedAccount)
                }
                AccountManager.addAccount(fullBedrockSession)

                if (containedAccount == AccountManager.selectedAccount) {
                    AccountManager.selectAccount(fullBedrockSession)
                }
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
