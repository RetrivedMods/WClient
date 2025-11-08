package com.retrivedmods.wclient.overlay.gui.clickgui

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrivedmods.wclient.game.*
import com.retrivedmods.wclient.ui.theme.ClickGUIColors
import com.retrivedmods.wclient.util.translatedSelf

@Composable
fun ModuleToggle(
    module: Module,
    onToggle: () -> Unit,
    onSettingsClick: (() -> Unit)? = null,
    isExpanded: Boolean = false,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (module.isEnabled) ClickGUIColors.ModuleEnabled.copy(alpha = 0.2f) 
                     else ClickGUIColors.ModuleDisabled,
        label = "module_background"
    )
    
    val textColor by animateColorAsState(
        targetValue = if (module.isEnabled) ClickGUIColors.AccentColor
                     else ClickGUIColors.PrimaryText,
        label = "module_text"
    )

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor, RoundedCornerShape(6.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onToggle() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = module.name.translatedSelf,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            if (onSettingsClick != null) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Module Settings",
                    tint = if (isExpanded) ClickGUIColors.AccentColor else ClickGUIColors.SecondaryText,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSettingsClick() }
                )
            }
        }

        if (isExpanded && module.values.isNotEmpty()) {
            ModuleSettingsSection(module = module)
        }
    }
}

@Composable
fun ModuleSettingsSection(module: Module) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .background(
                ClickGUIColors.PanelBackground.copy(alpha = 0.3f),
                RoundedCornerShape(4.dp)
            )
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        module.values.forEach { value ->
            when (value) {
                is BoolValue -> {
                    BooleanSetting(value = value)
                }
                is FloatValue -> {
                    FloatSetting(value = value)
                }
                is IntValue -> {
                    IntSetting(value = value)
                }
                is ListValue -> {
                    ListSetting(value = value)
                }
                is EnumValue<*> -> {
                    EnumSetting(value = value)
                }
                is StringValue -> {
                    StringSetting(value = value)
                }
            }
        }
    }
}

@Composable
fun BooleanSetting(value: BoolValue) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = value.name.translatedSelf,
            color = ClickGUIColors.PrimaryText,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = value.value,
            onCheckedChange = { value.value = it },
            modifier = Modifier.size(width = 32.dp, height = 20.dp),
            colors = SwitchDefaults.colors(
                checkedThumbColor = ClickGUIColors.AccentColor,
                checkedTrackColor = ClickGUIColors.AccentColor.copy(alpha = 0.5f),
                uncheckedThumbColor = ClickGUIColors.SecondaryText,
                uncheckedTrackColor = ClickGUIColors.SecondaryText.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun FloatSetting(value: FloatValue) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value.name.translatedSelf,
                color = ClickGUIColors.PrimaryText,
                fontSize = 12.sp
            )
            Text(
                text = "%.2f".format(value.value),
                color = ClickGUIColors.AccentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Slider(
            value = value.value,
            onValueChange = { value.value = it },
            valueRange = value.range,
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp),
            colors = SliderDefaults.colors(
                thumbColor = ClickGUIColors.AccentColor,
                activeTrackColor = ClickGUIColors.AccentColor,
                inactiveTrackColor = ClickGUIColors.SecondaryText.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun IntSetting(value: IntValue) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value.name.translatedSelf,
                color = ClickGUIColors.PrimaryText,
                fontSize = 12.sp
            )
            Text(
                text = value.value.toString(),
                color = ClickGUIColors.AccentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Slider(
            value = value.value.toFloat(),
            onValueChange = { value.value = it.toInt() },
            valueRange = value.range.start.toFloat()..value.range.endInclusive.toFloat(),
            steps = if (value.range.endInclusive - value.range.start > 1) value.range.endInclusive - value.range.start - 1 else 0,
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp),
            colors = SliderDefaults.colors(
                thumbColor = ClickGUIColors.AccentColor,
                activeTrackColor = ClickGUIColors.AccentColor,
                inactiveTrackColor = ClickGUIColors.SecondaryText.copy(alpha = 0.3f)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListSetting(value: ListValue) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value.name.translatedSelf,
            color = ClickGUIColors.PrimaryText,
            fontSize = 12.sp
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = value.value.name,
                onValueChange = { },
                readOnly = true,
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth()
                    .height(32.dp),
                textStyle = MaterialTheme.typography.bodySmall,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ClickGUIColors.AccentColor,
                    unfocusedBorderColor = ClickGUIColors.SecondaryText.copy(alpha = 0.3f),
                    focusedTextColor = ClickGUIColors.PrimaryText,
                    unfocusedTextColor = ClickGUIColors.PrimaryText
                ),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                value.listItems.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.name,
                                fontSize = 12.sp,
                                color = if (option == value.value) ClickGUIColors.AccentColor else ClickGUIColors.PrimaryText
                            )
                        },
                        onClick = {
                            expanded = false
                            value.value = option
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnumSetting(value: EnumValue<*>) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value.name.translatedSelf,
            color = ClickGUIColors.PrimaryText,
            fontSize = 12.sp
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = value.value.name,
                onValueChange = { },
                readOnly = true,
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth()
                    .height(32.dp),
                textStyle = MaterialTheme.typography.bodySmall,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ClickGUIColors.AccentColor,
                    unfocusedBorderColor = ClickGUIColors.SecondaryText.copy(alpha = 0.3f),
                    focusedTextColor = ClickGUIColors.PrimaryText,
                    unfocusedTextColor = ClickGUIColors.PrimaryText
                ),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                value.enumClass.enumConstants?.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.name,
                                fontSize = 12.sp,
                                color = if (option == value.value) ClickGUIColors.AccentColor else ClickGUIColors.PrimaryText
                            )
                        },
                        onClick = {
                            expanded = false
                            @Suppress("UNCHECKED_CAST")
                            (value as EnumValue<Enum<*>>).value = option as Enum<*>
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StringSetting(value: StringValue) {
    var textFieldValue by remember(value.value) { mutableStateOf(value.value) }

    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value.name.translatedSelf,
            color = ClickGUIColors.PrimaryText,
            fontSize = 12.sp
        )

        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { textFieldValue = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp),
            textStyle = MaterialTheme.typography.bodySmall,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ClickGUIColors.AccentColor,
                unfocusedBorderColor = ClickGUIColors.SecondaryText.copy(alpha = 0.3f),
                focusedTextColor = ClickGUIColors.PrimaryText,
                unfocusedTextColor = ClickGUIColors.PrimaryText,
                cursorColor = ClickGUIColors.AccentColor
            ),
            singleLine = true,
            placeholder = {
                Text(
                    text = "Enter value...",
                    fontSize = 11.sp,
                    color = ClickGUIColors.SecondaryText.copy(alpha = 0.7f)
                )
            }
        )

        LaunchedEffect(textFieldValue) {
            if (textFieldValue != value.value) {
                value.value = textFieldValue
            }
        }
    }
}

