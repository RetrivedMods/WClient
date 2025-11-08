package com.retrivedmods.wclient.router.main

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.retrivedmods.wclient.R
import com.retrivedmods.wclient.ui.theme.WColors
import com.retrivedmods.wclient.util.LocalSnackbarHostState
import com.retrivedmods.wclient.util.SnackbarHostStateScope
import com.retrivedmods.wclient.viewmodel.MainScreenViewModel

data class ServerInfo(
    val name: String,
    val iconRes: Int,
    val address: String,
    val port: Int,
    val hasSubServers: Boolean = false,
    val subServers: List<SubServerInfo> = emptyList()
)

data class SubServerInfo(
    val name: String,
    val region: String,
    val address: String,
    val port: Int,
    val isSelected: Boolean = false
)

val servers = listOf(
    ServerInfo(
        name = "The Hive",
        iconRes = R.drawable.hive_icon,
        address = "geo.hivebedrock.network",
        port = 19132
    ),
    ServerInfo(
        name = "Lifeboat",
        iconRes = R.drawable.lifeboat_icon,
        address = "play.lbsg.net",
        port = 19132
    ),
    ServerInfo(
        name = "CubeCraft",
        iconRes = R.drawable.cubecraft_icon,
        address = "play.cubecraft.net",
        port = 19132
    ),
    ServerInfo(
        name = "DonutSMP",
        iconRes = R.drawable.donutsmp_icon,
        address = "donutsmp.net",
        port = 19132,
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerPageContent() {
    SnackbarHostStateScope {
        val mainScreenViewModel: MainScreenViewModel = viewModel()
        val snackbarHostState = LocalSnackbarHostState.current
        var selectedServer by remember { mutableStateOf<ServerInfo?>(null) }
        var showSubServers by remember { mutableStateOf(false) }
        var selectedSubServer by remember { mutableStateOf<SubServerInfo?>(null) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Servers",
                                style = MaterialTheme.typography.headlineMedium,
                                color = WColors.OnSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = WColors.Background,
                        titleContentColor = WColors.OnSurface
                    )
                )
            },
            bottomBar = {
                SnackbarHost(
                    snackbarHostState,
                    modifier = Modifier.animateContentSize()
                )
            },
            containerColor = WColors.Background
        ) { padding ->
            Box {
                Column(
                    Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    servers.forEach { server ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (server.hasSubServers) {
                                        selectedServer = server
                                        showSubServers = true
                                    } else {
                                        selectedServer = server
                                        selectedSubServer = null
                                        showSubServers = false
                                        mainScreenViewModel.selectCaptureModeModel(
                                            mainScreenViewModel.captureModeModel.value.copy(
                                                serverHostName = server.address,
                                                serverPort = server.port
                                            )
                                        )
                                    }
                                }
                                .then(
                                    if (selectedServer == server) {
                                        Modifier.border(
                                            2.dp,
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.shapes.medium
                                        )
                                    } else Modifier
                                ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    painter = painterResource(server.iconRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = Color.Unspecified
                                )

                                Text(
                                    text = server.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }

                if (showSubServers && selectedServer?.hasSubServers == true) {
                    Dialog(onDismissRequest = { showSubServers = false }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    "Select Region and Server",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                selectedServer?.subServers?.groupBy { it.region }?.forEach { (region, subServers) ->
                                    Text(
                                        region,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )

                                    subServers.forEach { subServer ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedSubServer = subServer
                                                    mainScreenViewModel.selectCaptureModeModel(
                                                        mainScreenViewModel.captureModeModel.value.copy(
                                                            serverHostName = subServer.address,
                                                            serverPort = subServer.port
                                                        )
                                                    )
                                                    showSubServers = false
                                                }
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = selectedSubServer == subServer,
                                                onClick = null
                                            )
                                            Text(
                                                subServer.name,
                                                modifier = Modifier.padding(start = 8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}