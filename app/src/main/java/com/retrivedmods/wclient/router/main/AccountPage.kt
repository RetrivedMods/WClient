package com.retrivedmods.wclient.router.main

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import com.retrivedmods.wclient.ui.component.WGlassCard
import com.retrivedmods.wclient.ui.component.WFloatingActionButton
import com.retrivedmods.wclient.ui.theme.WColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import com.retrivedmods.wclient.R
import com.retrivedmods.wclient.game.AccountManager
import com.retrivedmods.wclient.ui.component.AuthWebView
import com.retrivedmods.wclient.util.LocalSnackbarHostState
import com.retrivedmods.wclient.util.SnackbarHostStateScope
import com.retrivedmods.wclient.util.getActivityWindow
import com.retrivedmods.wclient.util.getDialogWindow
import com.retrivedmods.wclient.util.windowFullScreen
import kotlinx.coroutines.launch
import net.raphimc.minecraftauth.step.bedrock.session.StepFullBedrockSession.FullBedrockSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountPageContent() {
    SnackbarHostStateScope {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        var showAddAccountDropDownMenu by remember { mutableStateOf(false) }
        var selectedAccountAction: FullBedrockSession? by remember { mutableStateOf(null) }
        var login: Boolean by remember { mutableStateOf(false) }
        val snackbarHostState = LocalSnackbarHostState.current

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(R.string.account),
                                style = MaterialTheme.typography.headlineMedium,
                                color = WColors.OnSurface
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                showAddAccountDropDownMenu = true
                            }
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null)
                        }
                        AddAccountDropDownMenu(
                            expanded = showAddAccountDropDownMenu,
                            onClick = {
                                showAddAccountDropDownMenu = false
                                login = true
                            }
                        ) {
                            showAddAccountDropDownMenu = false
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
                    modifier = Modifier
                        .animateContentSize()
                )
            },
            containerColor = WColors.Background
        ) {
            Box(
                Modifier
                    .padding(it)
                    .fillMaxSize()
            ) {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(AccountManager.accounts) { account ->
                        ListItem(
                            modifier = Modifier
                                .animateItem()
                                .clickable {
                                    if (AccountManager.selectedAccount != account) {
                                        AccountManager.selectAccount(account)
                                    } else {
                                        AccountManager.selectAccount(null)
                                    }
                                },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                            headlineContent = {
                                Text(account.mcChain.displayName)
                            },
                            supportingContent = {
                                Row(Modifier.fillMaxWidth()) {
                                    Text("Android")
                                    if (account == AccountManager.selectedAccount) {
                                        Text(
                                            stringResource(R.string.has_been_selected),
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                            },
                            trailingContent = {
                                IconButton(
                                    onClick = {
                                        selectedAccountAction = account
                                    }
                                ) {
                                    Icon(Icons.Rounded.MoreVert, contentDescription = null)
                                }

                                DropdownMenu(
                                    expanded = selectedAccountAction == account,
                                    onDismissRequest = { selectedAccountAction = null }
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                if (account == AccountManager.selectedAccount) stringResource(
                                                    R.string.unselect
                                                ) else stringResource(
                                                    R.string.select
                                                )
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                if (account == AccountManager.selectedAccount) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {
                                            if (AccountManager.selectedAccount != account) {
                                                AccountManager.selectAccount(account)
                                            } else {
                                                AccountManager.selectAccount(null)
                                            }
                                            selectedAccountAction = null
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(stringResource(R.string.delete))
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Outlined.DeleteOutline,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {
                                            AccountManager.removeAccount(account)
                                            if (account == AccountManager.selectedAccount) {
                                                AccountManager.selectAccount(null)
                                            }
                                            selectedAccountAction = null
                                        }
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }

        if (login) {
            AccountDialog { throwable: Throwable? ->
                login = false
                coroutineScope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    throwable?.let {
                        snackbarHostState.showSnackbar(
                            context.getString(R.string.fetch_account_failed, it.message)
                        )
                    } ?: snackbarHostState.showSnackbar(
                        context.getString(R.string.fetch_account_successfully)
                    )
                }
            }
        }

    }
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountDialog(
    callback: (Throwable?) -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(
            usePlatformDefaultWidth = true,
            decorFitsSystemWindows = false
        )
    ) {
        val activityWindow = getActivityWindow()
        val dialogWindow = getDialogWindow()

        SideEffect {
            windowFullScreen(activityWindow, dialogWindow)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(stringResource(R.string.add_account))
                    }
                )
            }
        ) {
            Column(
                Modifier
                    .padding(it)
                    .fillMaxSize()
            ) {
                AndroidView(
                    factory = { context ->
                        AuthWebView(context).also { authWebView ->
                            authWebView.callback = callback
                        }.also { authWebView ->
                            authWebView.addAccount()
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun AddAccountDropDownMenu(
    expanded: Boolean,
    onClick: () -> Unit,
    onDismissRequest: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {

        DropdownMenuItem(
            text = {
                val xboxDeviceCodeString = stringResource(R.string.xbox_device_code)
                Text(stringResource(R.string.login_in, xboxDeviceCodeString))
            },
            onClick = {
                onClick()
            }
        )
    }
}