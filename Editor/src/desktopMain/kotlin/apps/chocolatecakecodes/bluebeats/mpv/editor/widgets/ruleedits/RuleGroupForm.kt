package apps.chocolatecakecodes.bluebeats.mpv.editor.widgets.ruleedits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.RuleGroup
import apps.chocolatecakecodes.bluebeats.mpv.editor.widgets.LabeledCheckbox
import apps.chocolatecakecodes.bluebeats.mpv.editor.widgets.LabeledRadioButton
import apps.chocolatecakecodes.bluebeats.mpv.editor.widgets.ShareForm

/**
 * @return callback to be called when changes should be applied to LoadedFile
 */
@Composable
internal fun RuleGroupForm(rule: RuleGroup, negated: MutableState<Boolean>, allowEditShare: Boolean): () -> Unit {
    val share = remember { mutableStateOf(rule.share) }
    val shareVal = rememberUpdatedState(share.value)
    var name by remember { mutableStateOf(TextFieldValue(rule.name)) }
    var combineWithAnd by remember { mutableStateOf(rule.combineWithAnd) }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ShareForm(share, enabled = allowEditShare)
        LabeledCheckbox(
            "Negated",
            negated.value,
            enabled = allowEditShare,
            onChange = { negated.value = it },
        )
        HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))

        TextField(
            name,
            onValueChange = { name = it },
            label = { Text("Name") },
            singleLine = true,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Combine rules with", modifier = Modifier.padding(end = 4.dp))
            LabeledRadioButton("AND", combineWithAnd) { combineWithAnd = true }
            LabeledRadioButton("OR", !combineWithAnd) { combineWithAnd = false }
        }
    }

    return {
        rule.share = shareVal.value
        rule.name = name.text
        rule.combineWithAnd = combineWithAnd
    }
}
