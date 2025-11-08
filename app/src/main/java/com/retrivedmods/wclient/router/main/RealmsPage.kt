package com.retrivedmods.wclient.router.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.retrivedmods.wclient.game.AccountManager
import com.retrivedmods.wclient.service.RealmsManager
import com.retrivedmods.wclient.ui.component.WRealmsSection
import com.retrivedmods.wclient.ui.theme.WColors
import com.retrivedmods.wclient.viewmodel.MainScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealmsPageContent() {
    val mainScreenViewModel: MainScreenViewModel = viewModel()
    val realmsState by RealmsManager.realmsState.collectAsStateWithLifecycle()

    LaunchedEffect(AccountManager.selectedAccount) {
        val selectedAccount = AccountManager.selectedAccount
        println("RealmsPage: Selected account changed: ${selectedAccount?.mcChain?.displayName}")
        println("RealmsPage: Account has Realms support: ${selectedAccount?.realmsXsts != null}")
        RealmsManager.updateSession(selectedAccount)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Realms",
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WColors.Background
                )
            )
        },
        containerColor = WColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            WRealmsSection(
                realmsState = realmsState,
                onRealmSelect = { host, port ->
                    val portInt = port.toIntOrNull() ?: 19132
                    println("RealmsPage: Connecting to Realm at $host:$portInt")

                    val currentModel = mainScreenViewModel.captureModeModel.value
                    val updatedModel = currentModel.copy(
                        serverHostName = host,
                        serverPort = portInt
                    ).withAutoDetectedServerConfig()
                    mainScreenViewModel.selectCaptureModeModel(updatedModel)

                    println("RealmsPage: Updated game settings - Host: $host, Port: $portInt")
                },
                onRefresh = {
                    RealmsManager.refreshRealms()
                }
            )
        }
    }
}