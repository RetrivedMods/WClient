package com.inkclient.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inkclient.modules.resource.ResourcePack
import com.inkclient.modules.resource.ResourcePackRepository
import com.inkclient.ui.theme.InkColors

/**
 * ResourcePackPanel — ultra-clean dashboard style UI for GlobalResourcePackChanger.
 *
 * Features:
 * - Listing of available packs with path and subtitle.
 * - Flat, monochrome 'Apply' and 'Hot-Swap' buttons.
 * - Single-selection visual focus using high-contrast white.
 *
 * This composable intentionally uses minimal, tight spacing and flat styling.
 */
@Composable
fun ResourcePackPanel(
    modifier: Modifier = Modifier,
    onApply: (ResourcePack) -> Unit = {},
    onHotSwap: (ResourcePack) -> Unit = {}
) {
    val packs = remember { ResourcePackRepository.list() }
    val selectedIdState = remember { mutableStateOf(packs.firstOrNull()?.id) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(InkColors.VelvetBlack),
        color = InkColors.VelvetBlack
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Text(
                text = "Ink Client — Global Resource Packs",
                color = InkColors.SoftMatteWhite,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Divider(color = InkColors.DeepCharcoal, thickness = 1.dp)

            // Packs list
            LazyColumn(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(packs, key = { it.id }) { pack ->
                    val isSelected = (selectedIdState.value == pack.id)
                    val backgroundColor = if (isSelected) InkColors.DeepCharcoal else InkColors.VelvetBlack

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(backgroundColor)
                            .clickable { selectedIdState.value = pack.id }
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pack.displayName,
                                    color = if (isSelected) InkColors.SolidWhite else InkColors.SoftMatteWhite,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = pack.path,
                                    color = InkColors.SlateDarkGray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            // Action buttons
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(
                                    onClick = { onApply(pack) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = InkColors.SolidWhite)
                                ) {
                                    Text(text = "Apply", color = InkColors.SolidWhite)
                                }

                                TextButton(
                                    onClick = { onHotSwap(pack) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = InkColors.SolidWhite)
                                ) {
                                    Text(text = "Hot-Swap", color = InkColors.SolidWhite)
                                }
                            }
                        }
                    }
                }
            }

            Divider(color = InkColors.DeepCharcoal, thickness = 1.dp)

            // Footer / controls summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live loopback agent: 127.0.0.1",
                    color = InkColors.SlateDarkGray,
                    fontSize = 12.sp
                )

                Text(
                    text = "Hot-swap enabled — no reload required",
                    color = InkColors.SoftMatteWhite,
                    fontSize = 12.sp
                )
            }
        }
    }
}
