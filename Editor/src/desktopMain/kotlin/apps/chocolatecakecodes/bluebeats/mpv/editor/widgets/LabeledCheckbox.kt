package apps.chocolatecakecodes.bluebeats.mpv.editor.widgets

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun LabeledCheckbox(label: String, selected: Boolean, enabled: Boolean = true, modifier: Modifier = Modifier, onChange: (Boolean) -> Unit) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = selected, onCheckedChange = onChange, enabled = enabled)
        Text(text = label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 4.dp, bottom = 2.dp))
    }
}
