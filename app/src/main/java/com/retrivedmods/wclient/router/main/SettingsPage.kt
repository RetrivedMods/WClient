package com.retrivedmods.wclient.router.main

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Opacity
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.retrivedmods.wclient.R
import com.retrivedmods.wclient.game.ModuleManager
import com.retrivedmods.wclient.overlay.OverlayManager
import com.retrivedmods.wclient.ui.component.WGlassCard
import com.retrivedmods.wclient.ui.theme.WColors
import com.retrivedmods.wclient.util.LocalSnackbarHostState
import com.retrivedmods.wclient.util.SnackbarHostStateScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPageContent() {
    SnackbarHostStateScope {
        val context = LocalContext.current
        val snackbarHostState = LocalSnackbarHostState.current
        val scope = rememberCoroutineScope()
        val prefs = remember { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }

        var showOpacityDialog by rememberSaveable { mutableStateOf(false) }
        var showFileNameDialog by rememberSaveable { mutableStateOf(false) }
        var configFileName by rememberSaveable { mutableStateOf("") }

        var overlayOpacity by remember {
            mutableFloatStateOf(prefs.getFloat("overlay_opacity", 1f))
        }
        var shortcutOpacity by remember {
            mutableFloatStateOf(prefs.getFloat("shortcut_opacity", 1f))
        }

        val importConfigPicker = rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let {
                val ok = ModuleManager.importConfigFromFile(context, it)
                scope.launch {
                    snackbarHostState.showSnackbar(
                        if (ok) "Config imported successfully" else "Failed to import config"
                    )
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.settings),
                            style = MaterialTheme.typography.headlineMedium,
                            color = WColors.OnSurface
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = WColors.Background,
                        titleContentColor = WColors.OnSurface
                    )
                )
            },
            bottomBar = { SnackbarHost(LocalSnackbarHostState.current) },
            containerColor = WColors.Background
        ) { padding ->
            Column(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showOpacityDialog = true }
                ) {
                    Row(
                        Modifier.padding(15.dp),
                        horizontalArrangement = Arrangement.spacedBy(15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Opacity, null, modifier = Modifier.size(24.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.overlay_opacity_settings),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                stringResource(R.string.overlay_opacity_description),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Icon(
                            Icons.Rounded.Settings, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.scale(0.8f).size(20.dp)
                        )
                    }
                }

                WGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    glowColor = WColors.Secondary,
                    glowIntensity = 0.3f
                ) {
                    Row(
                        Modifier.padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Palette, null, tint = WColors.Secondary, modifier = Modifier.size(24.dp))
                        Column(Modifier.weight(1f)) {
                            Text("GUI Theme", style = MaterialTheme.typography.titleMedium, color = WColors.OnSurface)
                            Text(
                                "Classic overlay theme (default)",
                                style = MaterialTheme.typography.bodySmall,
                                color = WColors.OnSurfaceVariant
                            )
                        }
                        Text("Classic", style = MaterialTheme.typography.bodyMedium, color = WColors.Secondary)
                    }
                }

                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(15.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(15.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.SaveAlt, null, modifier = Modifier.size(24.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Config Management", style = MaterialTheme.typography.bodyLarge)
                                Text("Save and load module configurations", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        FilledTonalButton(
                            onClick = { importConfigPicker.launch("application/json") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.Upload, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Import Config")
                        }

                        FilledTonalButton(
                            onClick = { showFileNameDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.SaveAlt, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Export Config")
                        }
                    }
                }

                if (showFileNameDialog) {
                    BasicAlertDialog(
                        onDismissRequest = {
                            showFileNameDialog = false
                            configFileName = ""
                        },
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Surface(shape = AlertDialogDefaults.shape, tonalElevation = AlertDialogDefaults.TonalElevation) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text("Save Configuration", style = MaterialTheme.typography.headlineSmall)
                                OutlinedTextField(
                                    value = configFileName,
                                    onValueChange = { configFileName = it },
                                    label = { Text("Configuration Name") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    supportingText = { Text("The config will be saved as '$configFileName.json'") }
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(onClick = { showFileNameDialog = false; configFileName = "" }) {
                                        Text("Cancel")
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    FilledTonalButton(
                                        onClick = {
                                            if (configFileName.isNotBlank()) {
                                                val ok = ModuleManager.exportConfigToFile(context, configFileName)
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        if (ok) "Configuration saved successfully"
                                                        else "Failed to save configuration"
                                                    )
                                                }
                                            }
                                            showFileNameDialog = false
                                            configFileName = ""
                                        },
                                        enabled = configFileName.isNotBlank()
                                    ) { Text("Save") }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showOpacityDialog) {
            BasicAlertDialog(
                onDismissRequest = { showOpacityDialog = false },
                modifier = Modifier.padding(vertical = 24.dp)
            ) {
                Surface(shape = AlertDialogDefaults.shape, tonalElevation = AlertDialogDefaults.TonalElevation) {
                    Column(
                        Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(stringResource(R.string.overlay_opacity_settings), style = MaterialTheme.typography.headlineSmall)
                        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                            Column {
                                Text(stringResource(R.string.overlay_opacity), style = MaterialTheme.typography.bodyLarge)
                                Slider(
                                    value = overlayOpacity,
                                    onValueChange = {
                                        overlayOpacity = it
                                        prefs.edit { putFloat("overlay_opacity", it) }
                                        OverlayManager.updateOverlayOpacity(it)
                                    },
                                    valueRange = 0f..1f
                                )
                            }
                            Column {
                                Text(stringResource(R.string.shortcut_opacity), style = MaterialTheme.typography.bodyLarge)
                                Slider(
                                    value = shortcutOpacity,
                                    onValueChange = {
                                        shortcutOpacity = it
                                        prefs.edit { putFloat("shortcut_opacity", it) }
                                        OverlayManager.updateShortcutOpacity(it)
                                    },
                                    valueRange = 0f..1f
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
