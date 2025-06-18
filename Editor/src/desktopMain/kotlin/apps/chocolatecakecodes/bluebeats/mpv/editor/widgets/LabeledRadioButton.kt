package apps.chocolatecakecodes.bluebeats.mpv.editor.widgets

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
internal fun LabeledRadioButton(label: String, selected: Boolean, enabled: Boolean = true, onSelect: () -> Unit) {
    Row(modifier = Modifier.selectable(
        role = Role.RadioButton,
        enabled = enabled,
        selected = selected,
        onClick = onSelect
    )) {
        RadioButton(selected = selected, enabled = enabled, onClick = null)
        Text(text = label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
    }
}
