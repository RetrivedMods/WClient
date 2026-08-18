package com.retrivedmods.wclient.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.retrivedmods.wclient.auth.VerificationManager
import com.retrivedmods.wclient.game.ModuleManager
import com.retrivedmods.wclient.navigation.Navigation
import com.retrivedmods.wclient.ui.component.LoadingScreen
import com.retrivedmods.wclient.ui.component.VerificationDialog
import com.retrivedmods.wclient.ui.theme.WClientTheme
import com.retrivedmods.wclient.util.SoundUtil
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Toast.makeText(
                this,
                "Storage permissions granted - You can now export configs",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                this,
                "Storage permissions are required to export configs to external storage",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @SuppressLint("BatteryLife")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SoundUtil.load(applicationContext)


        ModuleManager.loadConfig()

        enableEdgeToEdge()
        setupImmersiveMode()
        checkBatteryOptimizations()

        requestStoragePermissions()

        setContent {
            WClientTheme {
                var showLoading by remember { mutableStateOf(true) }
                var showVerificationDialog by remember { mutableStateOf(false) }
                var verifying by remember { mutableStateOf(false) }
                var wclientId by remember { mutableStateOf("") }

                if (showLoading) {
                    LoadingScreen(
                        onDone = {
                            // WClient ID verification (ad-gate) disabled - go straight in.
                            showLoading = false
                        }
                    )
                } else if (showVerificationDialog) {
                    VerificationDialog(
                        wclientId = wclientId,
                        onVerifyClick = {
                            lifecycleScope.launch {
                                verifying = true
                                try {
                                    val verifyUrl = VerificationManager.requestVerification(
                                        this@MainActivity,
                                        wclientId
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

                                    VerificationManager.pollVerificationStatus(
                                        this@MainActivity,
                                        wclientId
                                    ) { verified, reason ->
                                        verifying = false
                                        if (verified) {
                                            showVerificationDialog = false
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

    private fun requestStoragePermissions() {
        if (hasStoragePermissions()) {
            return
        }

        val permissions = getRequiredStoragePermissions()
        storagePermissionLauncher.launch(permissions)
    }

    private fun hasStoragePermissions(): Boolean {
        val permissions = getRequiredStoragePermissions()
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun getRequiredStoragePermissions(): Array<String> {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            else -> {
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        VerificationManager.cancelAll()

        ModuleManager.saveConfig()
    }
}