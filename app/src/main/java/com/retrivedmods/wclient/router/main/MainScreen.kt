package com.retrivedmods.wclient.router.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.Help
import androidx.compose.material.icons.twotone.AccountBox
import androidx.compose.material.icons.twotone.AccountCircle
import androidx.compose.material.icons.twotone.Campaign
import androidx.compose.material.icons.twotone.Gesture
import androidx.compose.material.icons.twotone.Home
import androidx.compose.material.icons.twotone.House
import androidx.compose.material.icons.twotone.SendTimeExtension
import androidx.compose.material.icons.twotone.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.retrivedmods.wclient.R
import com.retrivedmods.wclient.ui.component.NavigationRailX
import com.retrivedmods.wclient.viewmodel.MainScreenViewModel

@Immutable
enum class MainScreenPages(
    val icon: @Composable () -> Unit,
    val label: @Composable () -> Unit,
    val content: @Composable () -> Unit
) {
    HomePage(
        icon = { Icon(Icons.TwoTone.House, contentDescription = null) },
        label = { Text(stringResource(R.string.home)) },
        content = { HomePageContent() }
    ),
    AccountPage(
        icon = { Icon(Icons.TwoTone.AccountBox, contentDescription = null) },
        label = { Text(stringResource(R.string.account)) },
        content = { AccountPageContent() }
    ),
    AboutPage(
        icon = { Icon(Icons.TwoTone.Campaign, contentDescription = null) },
        label = { Text(stringResource(R.string.about)) },
        content = { AboutPageContent() }
    ),
    TutorialPage(
        icon = { Icon(Icons.TwoTone.Gesture, contentDescription = null) },
        label = { Text(stringResource(R.string.tutorial)) },
        content = { TutorialPageContent() }
    ),
    SettingsPage(
        icon = { Icon(Icons.TwoTone.SendTimeExtension, contentDescription = null) },
        label = { Text(stringResource(R.string.settings)) },
        content = { SettingsPageContent() }
    )
}

@Composable
fun MainScreen() {
    val mainScreenViewModel: MainScreenViewModel = viewModel()
    val selectedPage by mainScreenViewModel.selectedPage.collectAsStateWithLifecycle()
    Row(
        Modifier
            .fillMaxSize()
    ) {
        NavigationRailX(
            alignment = Alignment.Top
        ) {
            MainScreenPages.entries.fastForEach { page ->
                NavigationRailItem(
                    selected = selectedPage === page,
                    onClick = {
                        if (selectedPage !== page) {
                            mainScreenViewModel.selectPage(page)
                        }
                    },
                    icon = page.icon,
                    label = page.label,
                    alwaysShowLabel = false
                )
            }
        }
        VerticalDivider()
        AnimatedContent(
            targetState = selectedPage,
            label = "animatedPage",
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer),
        ) { currentPage ->
            Box(Modifier.fillMaxSize()) {
                currentPage.content()
            }
        }
    }
}