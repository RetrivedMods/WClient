package com.retrivedmods.wclient.router.main

import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.retrivedmods.wclient.ui.component.*
import com.retrivedmods.wclient.R
import com.retrivedmods.wclient.ui.theme.WColors
import com.retrivedmods.wclient.util.LocalSnackbarHostState
import com.retrivedmods.wclient.util.SnackbarHostStateScope
import androidx.core.net.toUri
import com.retrivedmods.wclient.BuildConfig

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AboutPageContent() {
    SnackbarHostStateScope {
        val snackbarHostState = LocalSnackbarHostState.current
        val context = LocalContext.current

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(R.string.about),
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
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppInfoSection()
                LicenseSection()
                LegalSection()
                AcknowledgementsSection(context)
                SourceCodeSection(context)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun AppInfoSection() {
    WGlassCard(
        modifier = Modifier.fillMaxWidth(),
        glowColor = WColors.Primary,
        glowIntensity = 0.4f
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "WClient",
                style = MaterialTheme.typography.displaySmall,
                color = WColors.Primary
            )
            Text(
                text = "RetrivedMods",
                style = MaterialTheme.typography.titleMedium,
                color = WColors.OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = WColors.Primary.copy(alpha = 0.12f),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = "Version ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelLarge,
                    color = WColors.Primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun LicenseSection() {
    WGlassCard(
        modifier = Modifier.fillMaxWidth(),
        glowColor = WColors.Secondary,
        glowIntensity = 0.3f
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "License",
                style = MaterialTheme.typography.headlineSmall,
                color = WColors.OnSurface,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            HorizontalDivider(
                color = WColors.OnSurfaceVariant.copy(alpha = 0.2f)
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Permitted Uses",
                    style = MaterialTheme.typography.titleMedium,
                    color = WColors.Accent
                )
                Surface(
                    color = WColors.Accent.copy(alpha = 0.08f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BulletPoint("Personal use and modification")
                        BulletPoint("Creating content using WClient")
                        BulletPoint("Redistributing source code with GPLv3 license")
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Prohibited Uses",
                    style = MaterialTheme.typography.titleMedium,
                    color = WColors.Error
                )
                Surface(
                    color = WColors.Error.copy(alpha = 0.08f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BulletPoint("Distributing without source code and license")
                        BulletPoint("Claiming ownership of original source code")
                    }
                }
            }
        }
    }
}

@Composable
private fun BulletPoint(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            color = WColors.OnSurface
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = WColors.OnSurface
        )
    }
}

@Composable
private fun LegalSection() {
    WGlassCard(
        modifier = Modifier.fillMaxWidth(),
        glowColor = WColors.OnSurfaceVariant,
        glowIntensity = 0.2f
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Legal",
                style = MaterialTheme.typography.headlineSmall,
                color = WColors.OnSurface,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            HorizontalDivider(
                color = WColors.OnSurfaceVariant.copy(alpha = 0.2f)
            )

            LegalItem(
                title = "Disclaimer of Warranty",
                content = "This program is distributed under the GNU General Public License v3 (GPLv3). It is provided \"AS IS\", without any warranty of any kind, express or implied, including but not limited to the warranties of merchantability, fitness for a particular purpose, and noninfringement."
            )

            LegalItem(
                title = "Limitation of Liability",
                content = "In no event shall the author(s) or copyright holder(s) be liable for any claim, damages, or other liability, whether in an action of contract, tort, or otherwise, arising from, out of, or in connection with the software or the use or other dealings in the software."
            )

            LegalItem(
                title = "Intended Use",
                content = "This software is intended solely for educational and research purposes. Any use of this program that violates applicable laws, terms of service, or causes harm to others is strictly unintended and the responsibility of the user."
            )
        }
    }
}

@Composable
private fun LegalItem(title: String, content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = WColors.OnSurface
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodySmall,
            color = WColors.OnSurfaceVariant,
            lineHeight = MaterialTheme.typography.bodySmall.lineHeight.times(1.5f)
        )
    }
}

@Composable
private fun AcknowledgementsSection(context: android.content.Context) {
    WGlassCard(
        modifier = Modifier.fillMaxWidth(),
        glowColor = WColors.Primary,
        glowIntensity = 0.25f
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Acknowledgements",
                style = MaterialTheme.typography.headlineSmall,
                color = WColors.OnSurface,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            HorizontalDivider(
                color = WColors.OnSurfaceVariant.copy(alpha = 0.2f)
            )

            Text(
                text = "Special thanks to the contributors who made this project possible.",
                style = MaterialTheme.typography.bodyMedium,
                color = WColors.OnSurfaceVariant
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ContributorItem(
                    name = "一剪沐橙",
                    description = "Core contributor",
                    githubUrl = "https://github.com/mucute-qwq",
                    context = context
                )

                ContributorItem(
                    name = "radiantByte",
                    description = "Project contributor",
                    githubUrl = null,
                    context = context
                )
            }
        }
    }
}

@Composable
private fun ContributorItem(
    name: String,
    description: String,
    githubUrl: String?,
    context: android.content.Context
) {
    Surface(
        onClick = if (githubUrl != null) {
            {
                val intent = Intent(Intent.ACTION_VIEW, githubUrl.toUri())
                context.startActivity(intent)
            }
        } else {
            {}
        },
        color = WColors.Primary.copy(alpha = 0.06f),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    color = WColors.OnSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = WColors.OnSurfaceVariant
                )
            }
            if (githubUrl != null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "View on GitHub",
                    tint = WColors.OnSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SourceCodeSection(context: android.content.Context) {
    WGlassCard(
        onClick = {
            val intent = Intent(
                Intent.ACTION_VIEW,
                "https://github.com/RetrivedMods/WClient".toUri()
            )
            context.startActivity(intent)
        },
        modifier = Modifier.fillMaxWidth(),
        glowColor = WColors.Secondary,
        glowIntensity = 0.3f
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Source Code",
                    style = MaterialTheme.typography.titleMedium,
                    color = WColors.OnSurface
                )
                Text(
                    text = "View on GitHub",
                    style = MaterialTheme.typography.bodySmall,
                    color = WColors.OnSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = "Open GitHub",
                tint = WColors.Secondary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}