package com.retrivedmods.wclient.game

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.retrivedmods.wclient.R
import com.retrivedmods.wclient.overlay.OverlayManager
import com.retrivedmods.wclient.util.translatedSelf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

private val moduleCache = HashMap<ModuleCategory, List<Module>>()

private fun fetchCachedModules(category: ModuleCategory): List<Module> {
    val cached = moduleCache[category] ?: ModuleManager.modules
        .filter { !it.private && it.category === category }
    moduleCache[category] = cached
    return cached
}

@Composable
fun ModuleContent(moduleCategory: ModuleCategory) {
    var modules: List<Module>? by remember(moduleCategory) { mutableStateOf(moduleCache[moduleCategory]) }

    LaunchedEffect(modules) {
        if (modules == null) withContext(Dispatchers.IO) { modules = fetchCachedModules(moduleCategory) }
    }

    Crossfade(targetState = modules) { list ->
        if (list != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(list.size) { i -> ModuleCard(list[i]) }
            }
        } else {
            Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun ModuleCard(module: Module) {
    val values = module.values
    val bg by animateColorAsState(
        targetValue = if (module.isExpanded) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.background,
        label = "moduleBg"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { module.isExpanded = !module.isExpanded },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.outlinedCardColors(containerColor = bg)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(10.dp)
            ) {
                Text(
                    module.name.translatedSelf,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (module.isExpanded)
                        contentColorFor(MaterialTheme.colorScheme.primary)
                    else
                        contentColorFor(MaterialTheme.colorScheme.background)
                )
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = module.isEnabled,
                    onCheckedChange = { module.isEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedBorderColor = if (module.isExpanded) MaterialTheme.colorScheme.surface else Color.Transparent,
                        uncheckedTrackColor = Color.Transparent,
                        uncheckedBorderColor = if (module.isExpanded) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.outline,
                        uncheckedThumbColor = if (module.isExpanded) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .width(52.dp)
                        .height(32.dp)
                )
            }

            if (module.isExpanded) {
                values.fastForEach {
                    when (it) {
                        is BoolValue -> BoolValueContent(it)
                        is FloatValue -> FloatValueContent(it)
                        is IntValue -> IntValueContent(it)
                        is ListValue -> ChoiceValueContent(it)
                        is EnumValue<*> -> EnumValueContent(it)
                        is StringValue -> StringValueContent(it)
                    }
                }
                ShortcutContent(module)
            }
        }
    }
}

@Composable
private fun ChoiceValueContent(value: ListValue) {
    Column(Modifier.padding(start = 10.dp, end = 10.dp, bottom = 5.dp)) {
        Text(
            value.name.translatedSelf,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.surface
        )
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            value.listItems.forEach { item ->
                ElevatedFilterChip(
                    selected = value.value == item,
                    onClick = { if (value.value != item) value.value = item },
                    label = { Text(item.name.translatedSelf) },
                    modifier = Modifier.height(30.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.outlineVariant,
                        selectedContainerColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(Modifier.width(6.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FloatValueContent(value: FloatValue) {
    Column(Modifier.padding(start = 10.dp, end = 10.dp, bottom = 5.dp)) {
        Row {
            Text(
                value.name.translatedSelf,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.surface
            )
            Spacer(Modifier.weight(1f))
            Text(
                value.value.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.surface
            )
        }

        val colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.surface,
            activeTrackColor = MaterialTheme.colorScheme.surface,
            activeTickColor = MaterialTheme.colorScheme.surface,
            inactiveTickColor = MaterialTheme.colorScheme.outlineVariant,
            inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
        )

        val animated by animateFloatAsState(
            targetValue = value.value,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "floatSlider"
        )

        Slider(
            value = animated,
            onValueChange = {
                val rounded = ((it * 100.0).roundToInt() / 100.0).toFloat()
                if (value.value != rounded) value.value = rounded
            },
            valueRange = value.range,
            colors = colors
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IntValueContent(value: IntValue) {
    Column(Modifier.padding(start = 10.dp, end = 10.dp, bottom = 5.dp)) {
        Row {
            Text(
                value.name.translatedSelf,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.surface
            )
            Spacer(Modifier.weight(1f))
            Text(
                value.value.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.surface
            )
        }

        val colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.surface,
            activeTrackColor = MaterialTheme.colorScheme.surface,
            activeTickColor = MaterialTheme.colorScheme.surface,
            inactiveTickColor = MaterialTheme.colorScheme.outlineVariant,
            inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
        )

        val animated by animateFloatAsState(
            targetValue = value.value.toFloat(),
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "intSlider"
        )

        Slider(
            value = animated,
            onValueChange = {
                val next = it.roundToInt()
                if (value.value != next) value.value = next
            },
            valueRange = value.range.toFloatRange(),
            colors = colors
        )
    }
}

@Composable
private fun BoolValueContent(value: BoolValue) {
    Row(
        Modifier
            .padding(start = 10.dp, end = 10.dp, bottom = 5.dp)
            .toggleable(
                value = value.value,
                interactionSource = null,
                indication = null
            ) { value.value = it }
    ) {
        Text(
            value.name.translatedSelf,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.surface
        )
        Spacer(Modifier.weight(1f))
        Checkbox(
            checked = value.value,
            onCheckedChange = null,
            modifier = Modifier.padding(0.dp),
            colors = CheckboxDefaults.colors(
                uncheckedColor = MaterialTheme.colorScheme.surface,
                checkedColor = MaterialTheme.colorScheme.surface,
                checkmarkColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun ShortcutContent(module: Module) {
    Row(
        Modifier
            .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
            .toggleable(
                value = module.isShortcutDisplayed,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                module.isShortcutDisplayed = it
                if (it) OverlayManager.showOverlayWindow(module.overlayShortcutButton)
                else OverlayManager.dismissOverlayWindow(module.overlayShortcutButton)
            }
    ) {
        Text(
            stringResource(R.string.shortcut),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.surface
        )
        Spacer(Modifier.weight(1f))
        Checkbox(
            checked = module.isShortcutDisplayed,
            onCheckedChange = null,
            modifier = Modifier.padding(0.dp),
            colors = CheckboxDefaults.colors(
                uncheckedColor = MaterialTheme.colorScheme.surface,
                checkedColor = MaterialTheme.colorScheme.surface,
                checkmarkColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun <T : Enum<T>> EnumValueContent(value: EnumValue<T>) {
    Column(Modifier.padding(start = 10.dp, end = 10.dp, bottom = 5.dp)) {
        Text(
            value.name.translatedSelf,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.surface
        )
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            value.enumClass.enumConstants?.forEach { option ->
                ElevatedFilterChip(
                    selected = value.value == option,
                    onClick = { if (value.value != option) value.value = option },
                    label = { Text(option.name.translatedSelf) },
                    modifier = Modifier.height(30.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.outlineVariant,
                        selectedContainerColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(Modifier.width(6.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StringValueContent(value: StringValue) {
    Column(Modifier.padding(start = 10.dp, end = 10.dp, bottom = 5.dp)) {
        Text(
            value.name.translatedSelf,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.surface
        )
        OutlinedTextField(
            value = value.value,
            onValueChange = { value.value = it },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedTextColor = MaterialTheme.colorScheme.surface,
                unfocusedTextColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}

private fun IntRange.toFloatRange() = first.toFloat()..last.toFloat()
