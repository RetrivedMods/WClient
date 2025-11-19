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
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.retrivedmods.wclient.navigation.Navigation
import com.retrivedmods.wclient.ui.component.LoadingScreen
import androidx.compose.runtime.*
import com.retrivedmods.wclient.ui.theme.WClientTheme
import com.retrivedmods.wclient.auth.VerificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                    LoadingScreen(onDone = {
                        lifecycleScope.launch {

                            if (VerificationManager.isAuthorized(this@MainActivity)) {
                                withContext(Dispatchers.Main) { showLoading = false }
                                return@launch
                            }


                            withContext(Dispatchers.Main) { verifying = true }

                            try {

                                val (token, realUrl, verifyUrl) = VerificationManager.requestVerificationDirect(this@MainActivity, short = true)
                                VerificationManager.openInAppBrowser(this@MainActivity, verifyUrl)


                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@MainActivity, "Complete verification in the browser, then return to this app.", Toast.LENGTH_LONG).show()
                                }


                                VerificationManager.pollTokenStatus(this@MainActivity, token) { verified, reason ->
                                    lifecycleScope.launch {
                                        withContext(Dispatchers.Main) {
                                            verifying = false
                                            if (verified) {
                                                showLoading = false
                                                Toast.makeText(this@MainActivity, "Device verified — welcome!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(this@MainActivity, "Verification failed: ${reason ?: "unknown"}", Toast.LENGTH_LONG).show()

                                                showLoading = false
                                            }
                                        }
                                    }
                                }
                            } catch (t: Throwable) {
                                withContext(Dispatchers.Main) {
                                    verifying = false
                                    showLoading = false
                                    Toast.makeText(this@MainActivity, "Verification request failed: ${t.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    })
                } else {
                    if (verifying) {

                        LoadingScreen(onDone = { /* no-op */ })
                    } else {

                        Navigation()
                    }
                }
            }
        }
    }

    private fun setupImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    @SuppressLint("BatteryLife")
    private fun checkBatteryOptimizations() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent().apply {
                action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                data = "package:$packageName".toUri()
            }
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        VerificationManager.cancelAll()
    }
}
