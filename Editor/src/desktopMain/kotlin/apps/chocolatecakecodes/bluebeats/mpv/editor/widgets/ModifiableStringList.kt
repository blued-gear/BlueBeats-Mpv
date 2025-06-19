package apps.chocolatecakecodes.bluebeats.mpv.editor.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
internal fun ModifiableStringList(values: SnapshotStateList<String>, newItemFactory: suspend () -> String? = { "" }) {
    val factoryCoroutineScope = rememberCoroutineScope()

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        values.forEachIndexed { idx, str ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = str,
                    onValueChange = { values[idx] = it },
                    modifier = Modifier.padding(end = 12.dp).weight(1.0f),
                    singleLine = true,
                )
                IconButton(
                    onClick = { values.removeAt(idx) }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "remove item",
                    )
                }
            }
        }

        IconButton(
            modifier = Modifier.padding(top = 8.dp),
            onClick = {
                factoryCoroutineScope.launch {
                    newItemFactory()?.let { values.add(it) }
                }
            }
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "add item",
            )
        }
    }
}
