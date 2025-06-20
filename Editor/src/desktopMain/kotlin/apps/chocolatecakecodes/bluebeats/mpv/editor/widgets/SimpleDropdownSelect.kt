package apps.chocolatecakecodes.bluebeats.mpv.editor.widgets

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T> SimpleDropdownSelect(
    label: String,
    options: List<T>,
    selected: MutableState<T>,
    enabled: Boolean = true,
    itemTitle: (T) -> String = { it.toString() }
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        TextField(
            value = itemTitle(selected.value),
            label = { Text(label, fontWeight = FontWeight.Bold) },
            enabled = enabled,
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            readOnly = true,
            onValueChange = {},
            textStyle = TextStyle.Default.copy(fontSize = 14.sp, fontWeight=  FontWeight.Light),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(
                focusedIndicatorColor = Transparent,
                unfocusedIndicatorColor = Transparent
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(itemTitle(option)) },
                    onClick = {
                        selected.value = option
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T> SimpleDropdownSelectN(
    label: String,
    options: List<T>,
    selected: MutableState<T?>,
    enabled: Boolean = true,
    itemTitle: (T) -> String = { it.toString() }
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = enabled && it },
    ) {
        TextField(
            value = selected.value?.let { itemTitle(it) } ?: "",
            label = { Text(label, fontWeight = FontWeight.Bold) },
            enabled = enabled,
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            readOnly = true,
            onValueChange = {},
            textStyle = TextStyle.Default.copy(fontSize = 14.sp, fontWeight=  FontWeight.Light),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(
                focusedIndicatorColor = Transparent,
                unfocusedIndicatorColor = Transparent
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(itemTitle(option)) },
                    onClick = {
                        selected.value = option
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}
