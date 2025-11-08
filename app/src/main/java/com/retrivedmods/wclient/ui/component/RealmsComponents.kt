package com.retrivedmods.wclient.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.retrivedmods.wclient.model.RealmWorld
import com.retrivedmods.wclient.model.RealmConnectionDetails
import com.retrivedmods.wclient.model.RealmState
import com.retrivedmods.wclient.model.RealmsLoadingState
import com.retrivedmods.wclient.service.RealmsManager
import com.retrivedmods.wclient.ui.theme.WColors

@Composable
fun WRealmsSection(
    realmsState: RealmsLoadingState,
    onRealmSelect: (String, String) -> Unit,
    onRefresh: () -> Unit = {}
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Cloud,
                contentDescription = null,
                tint = WColors.Primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "My Realms",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = WColors.OnBackground
            )

            Spacer(modifier = Modifier.weight(1f))

            if (realmsState is RealmsLoadingState.Success || realmsState is RealmsLoadingState.Error) {
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh Realms",
                        tint = WColors.Primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        when (realmsState) {
            is RealmsLoadingState.Loading -> {
                RealmsLoadingCard()
            }
            is RealmsLoadingState.Success -> {
                if (realmsState.realms.isEmpty()) {
                    RealmsEmptyCard()
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        realmsState.realms.forEach { realm ->
                            RealmCard(
                                realm = realm,
                                onRealmSelect = onRealmSelect
                            )
                        }
                    }
                }
            }
            is RealmsLoadingState.Error -> {
                RealmsErrorCard(
                    message = realmsState.message,
                    onRetry = onRefresh
                )
            }
            is RealmsLoadingState.NotAvailable -> {
                RealmsNotAvailableCard()
            }
            is RealmsLoadingState.NoAccount -> {
                RealmsNoAccountCard()
            }
        }
    }
}

@Composable
private fun RealmCard(
    realm: RealmWorld,
    onRealmSelect: (String, String) -> Unit
) {
    var connectionDetails by remember { mutableStateOf<RealmConnectionDetails?>(null) }
    var isLoadingConnection by remember { mutableStateOf(false) }

    Card(
        onClick = {
            if (connectionDetails?.error == null && !isLoadingConnection && !realm.expired && realm.state == RealmState.OPEN) {
                if (connectionDetails == null || connectionDetails!!.isExpired()) {
                    isLoadingConnection = true
                    RealmsManager.getRealmConnectionDetails(realm.id) { details ->
                        isLoadingConnection = false
                        connectionDetails = details

                        if (details.error == null && !details.isLoading && details.address.isNotBlank() && details.port > 0) {
                            onRealmSelect(details.address, details.port.toString())
                        }
                    }
                } else {
                    if (connectionDetails!!.address.isNotBlank() && connectionDetails!!.port > 0) {
                        onRealmSelect(connectionDetails!!.address, connectionDetails!!.port.toString())
                    }
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = WColors.Surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = realm.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = WColors.OnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (realm.motd.isNotBlank()) {
                        Text(
                            text = realm.motd,
                            style = MaterialTheme.typography.bodySmall,
                            color = WColors.OnSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "Owner: ${realm.ownerName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = WColors.OnSurfaceVariant
                    )
                }

                RealmStatusChip(
                    state = realm.state,
                    expired = realm.expired
                )
            }

            when {
                isLoadingConnection || connectionDetails?.isLoading == true -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = WColors.Primary
                        )
                        Text(
                            text = "Getting connection details...",
                            style = MaterialTheme.typography.bodySmall,
                            color = WColors.OnSurfaceVariant
                        )
                    }
                }
                connectionDetails?.error != null -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = connectionDetails!!.error!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                connectionDetails != null && !connectionDetails!!.isLoading && connectionDetails!!.error == null && connectionDetails!!.address.isNotBlank() && connectionDetails!!.port > 0 -> {
                    Text(
                        text = "${connectionDetails!!.address}:${connectionDetails!!.port}",
                        style = MaterialTheme.typography.bodySmall,
                        color = WColors.OnSurfaceVariant
                    )
                }
                else -> {
                    Text(
                        text = "Tap to connect",
                        style = MaterialTheme.typography.bodySmall,
                        color = WColors.Primary
                    )
                }
            }
        }
    }
}

@Composable
private fun RealmStatusChip(
    state: RealmState,
    expired: Boolean
) {
    val (text, color) = when {
        expired -> "Expired" to MaterialTheme.colorScheme.error
        state == RealmState.OPEN -> "Open" to WColors.Primary
        state == RealmState.CLOSED -> "Closed" to WColors.OnSurfaceVariant
        else -> "Unknown" to WColors.OnSurfaceVariant
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun RealmsLoadingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = WColors.Surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = WColors.Primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Loading your Realms...",
                style = MaterialTheme.typography.bodyMedium,
                color = WColors.OnSurface
            )
        }
    }
}

@Composable
private fun RealmsEmptyCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = WColors.Surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Cloud,
                contentDescription = "No Realms",
                tint = WColors.OnSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "No Realms Available",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = WColors.OnSurface
            )
            Text(
                text = "You don't have access to any Realms yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = WColors.OnSurfaceVariant
            )
        }
    }
}

@Composable
private fun RealmsErrorCard(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = WColors.Surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "Failed to Load Realms",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = WColors.OnSurface
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = WColors.OnSurfaceVariant
            )

            OutlinedButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun RealmsNotAvailableCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = WColors.Surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Cloud,
                contentDescription = "Not Available",
                tint = WColors.OnSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "Realms Not Available",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = WColors.OnSurface
            )
            Text(
                text = "Your account doesn't have Realms support enabled. Please re-authenticate your Microsoft account to enable Realms access.",
                style = MaterialTheme.typography.bodyMedium,
                color = WColors.OnSurfaceVariant
            )

            Text(
                text = "Go to Account → Add Account to re-authenticate",
                style = MaterialTheme.typography.bodySmall,
                color = WColors.Primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun RealmsNoAccountCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = WColors.Surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = "No Account",
                tint = WColors.OnSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "Microsoft Account Required",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = WColors.OnSurface
            )
            Text(
                text = "Connect your Microsoft account to access Realms.",
                style = MaterialTheme.typography.bodyMedium,
                color = WColors.OnSurfaceVariant
            )
        }
    }
}