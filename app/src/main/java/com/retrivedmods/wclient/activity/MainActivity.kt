package com.retrivedmods.wclient.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.retrivedmods.wclient.auth.VerificationManager
import com.retrivedmods.wclient.navigation.Navigation
import com.retrivedmods.wclient.ui.component.LoadingScreen
import com.retrivedmods.wclient.ui.theme.WClientTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    @SuppressLint("BatteryLife")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setupImmersiveMode()
        checkBatteryOptimizations()

        setContent {
            WClientTheme {

                var showLoading by remember { mutableStateOf(true) }
                var verifying by remember { mutableStateOf(false) }

                if (showLoading) {
                    LoadingScreen(
                        onDone = {
                            lifecycleScope.launch {


                                if (VerificationManager.isAuthorized(this@MainActivity)) {
                                    showLoading = false
                                    return@launch
                                }

                                verifying = true

                                try {

                                    val (token, _, verifyUrl) =
                                        VerificationManager.requestVerificationDirect(
                                            this@MainActivity,
                                            short = true
                                        )


                                    VerificationManager.openInAppBrowser(
                                        this@MainActivity,
                                        verifyUrl
                                    )

                                    Toast.makeText(
                                        this@MainActivity,
                                        "Complete verification in the browser, then return to this app.",
                                        Toast.LENGTH_LONG
                                    ).show()

                                  
                                    VerificationManager.pollTokenStatus(
                                        this@MainActivity,
                                        token
                                    ) { verified, reason ->

                                        verifying = false
                                        showLoading = false

                                        if (verified) {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Welcome - You are now verified!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Verification failed: ${reason ?: "unknown"}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }

                                } catch (t: Throwable) {
                                    verifying = false
                                    showLoading = false
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Verification request failed: ${t.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    )
                } else {
                    if (verifying) {
                        LoadingScreen(onDone = {})
                    } else {
                        Navigation()
                    }
                }
            }
        }
    }

    private fun setupImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    @SuppressLint("BatteryLife")
    private fun checkBatteryOptimizations() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            startActivity(
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = "package:$packageName".toUri()
                }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        VerificationManager.cancelAll()
    }
}
