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
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun ModifiableStringList(values: SnapshotStateList<String>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        values.forEachIndexed { idx, str ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = str,
                    onValueChange = { values[idx] = it },
                    modifier = Modifier.padding(end = 12.dp)
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
            onClick = { values.add("") }
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "add item",
            )
        }
    }
}
