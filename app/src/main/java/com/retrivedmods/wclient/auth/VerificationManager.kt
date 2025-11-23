package com.retrivedmods.wclient.auth

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder
import java.util.*

object VerificationManager {
    private const val TAG = "VerifyNet"
    private const val PREFS = "wclient_prefs"
    private const val KEY_DEVICE_ID = "device_id"
    private const val KEY_VERIFIED_UNTIL = "verified_until"
    private const val KEY_CURRENT_TOKEN = "current_token"


    private const val BASE_VERIFY_URL = "https://retrivedmods.online/LV/verify.php"

    private val client: OkHttpClient = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .addInterceptor { chain ->
            val req = chain.request()
            Log.d(TAG, "REQ: ${req.method} ${req.url}")
            val resp = chain.proceed(req)
            val loc = resp.header("Location")
            Log.d(TAG, "RESP: ${resp.code} ${resp.message} -> ${resp.request.url}")
            if (loc != null) Log.d(TAG, "Redirect Location: $loc")
            resp
        }
        .build()

    private val jsonMedia = "application/json; charset=utf-8".toMediaType()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getDeviceId(ctx: Context): String {
        val p = prefs(ctx)
        var id = p.getString(KEY_DEVICE_ID, null)
        if (id.isNullOrBlank()) {
            id = UUID.randomUUID().toString()
            p.edit().putString(KEY_DEVICE_ID, id).apply()
        }
        return id
    }

    private fun setVerifiedUntil(ctx: Context, epochSeconds: Long) {
        prefs(ctx).edit().putLong(KEY_VERIFIED_UNTIL, epochSeconds).apply()
    }

    fun getVerifiedUntil(ctx: Context): Long =
        prefs(ctx).getLong(KEY_VERIFIED_UNTIL, 0L)

    private fun setCurrentToken(ctx: Context, token: String?) {
        prefs(ctx).edit().putString(KEY_CURRENT_TOKEN, token).apply()
    }

    fun isAuthorized(ctx: Context): Boolean {
        val now = System.currentTimeMillis() / 1000L
        return getVerifiedUntil(ctx) > now
    }


    suspend fun requestVerificationDirect(ctx: Context, short: Boolean = false): Triple<String, String, String> =
        withContext(Dispatchers.IO) {
            val deviceId = getDeviceId(ctx)
            val payload = JSONObject().put("device_id", deviceId).toString()
            val reqBody = payload.toRequestBody(jsonMedia)
            val url = if (short) "$BASE_VERIFY_URL?action=request&short=1" else "$BASE_VERIFY_URL?action=request"
            val req = Request.Builder().url(url).post(reqBody).build()

            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) throw Exception("Server ${resp.code}")
                val body = resp.body?.string() ?: throw Exception("Empty response")
                val j = JSONObject(body)
                val token = j.optString("token", "")
                val real = j.optString("real_verify_url", j.optString("verify_url", ""))
                val verify = j.optString("verify_url", real)

                if (token.isBlank() || real.isBlank()) throw Exception("Invalid response")
                setCurrentToken(ctx, token)
                return@withContext Triple(token, real, verify)
            }
        }

    fun openInAppBrowser(activity: Activity, verifyUrl: String) {
        try {
            if (verifyUrl.isBlank()) {
                Log.w(TAG, "openInAppBrowser: empty URL")
                return
            }
            if (!verifyUrl.startsWith("http://") && !verifyUrl.startsWith("https://")) {
                Log.w(TAG, "openInAppBrowser: non-http URL, falling back to external: $verifyUrl")
                openInExternalBrowser(activity, verifyUrl)
                return
            }
            val builder = CustomTabsIntent.Builder()
            builder.setShowTitle(true)
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(activity, Uri.parse(verifyUrl))
            Log.d(TAG, "Launched CustomTab for $verifyUrl")
        } catch (e: ActivityNotFoundException) {
            Log.w(TAG, "CustomTab Activity not found - fallback to external", e)
            openInExternalBrowser(activity, verifyUrl)
        } catch (t: Throwable) {
            Log.w(TAG, "CustomTab failed - fallback to external", t)
            openInExternalBrowser(activity, verifyUrl)
        }
    }

    fun openInExternalBrowser(activity: Activity, url: String) {
        try {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity.startActivity(Intent.createChooser(i, "Open link"))
            Log.d(TAG, "Launched external browser for $url")
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "No browser to open url", e)
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to open external browser", t)
        }
    }

    fun pollTokenStatus(ctx: Context, token: String, onComplete: (Boolean, String?) -> Unit) {
        scope.launch {
            try {
                val pollIntervalMs = 3000L
                val start = System.currentTimeMillis()
                val maxDurationMs = 4L * 60L * 60L * 1000L + 10_000L
                while (true) {
                    val url = "$BASE_VERIFY_URL?action=status&token=${URLEncoder.encode(token, "utf-8")}"
                    val req = Request.Builder().url(url).get().build()
                    client.newCall(req).execute().use { resp ->
                        if (!resp.isSuccessful) {
                            Log.d(TAG, "Status call returned ${resp.code}; will retry")
                        } else {
                            val s = resp.body?.string() ?: ""
                            val j = JSONObject(s)
                            val exists = j.optBoolean("exists", false)
                            val verified = j.optBoolean("verified", false)
                            val expiresAt = j.optLong("expires_at", 0L)
                            if (!exists) {
                                withContext(Dispatchers.Main) { onComplete(false, "token expired or not found") }
                                return@launch
                            }
                            if (verified) {
                                val verifiedUntil = if (expiresAt > 0) expiresAt else (System.currentTimeMillis() / 1000L + 4 * 60 * 60)
                                setVerifiedUntil(ctx, verifiedUntil)
                                setCurrentToken(ctx, null)
                                withContext(Dispatchers.Main) { onComplete(true, null) }
                                return@launch
                            }
                        }
                    }
                    if (System.currentTimeMillis() - start > maxDurationMs) {
                        withContext(Dispatchers.Main) { onComplete(false, "timed out") }
                        return@launch
                    }
                    delay(pollIntervalMs)
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Polling error", t)
                withContext(Dispatchers.Main) { onComplete(false, t.message ?: "error") }
            }
        }
    }

    fun cancelAll() {
        scope.cancel()
    }
}
