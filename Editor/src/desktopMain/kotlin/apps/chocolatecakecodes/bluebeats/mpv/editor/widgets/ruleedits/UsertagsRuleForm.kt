package apps.chocolatecakecodes.bluebeats.mpv.editor.widgets.ruleedits

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.UsertagsRule
import apps.chocolatecakecodes.bluebeats.mpv.editor.LoadedFile
import apps.chocolatecakecodes.bluebeats.mpv.editor.widgets.*

/**
 * @return callback to be called when changes should be applied to LoadedFile
 */
@Composable
internal fun UsertagsRuleForm(rule: UsertagsRule, negated: MutableState<Boolean>): () -> Unit {
    val availableUsertags = remember { LoadedFile.mediaLib.existingUsertags.sorted() }
    val share = remember { mutableStateOf(rule.share) }
    val shareVal = rememberUpdatedState(share.value)
    var name by remember { mutableStateOf(TextFieldValue(rule.name)) }
    var combineWithAnd by remember { mutableStateOf(rule.combineWithAnd) }
    val tags = remember { rule.getTags().toMutableStateList() }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ShareForm(share)
        LabeledCheckbox(
            "Negated",
            negated.value,
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

        Text("Tags:")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.onSurface)
                .padding(all = 8.dp)
        ) {
            ModifiableWidgetList(
                tags,
                { "" }
            ) { tag, idx, modifier ->
                SearchableDropdownEdit(
                    "",
                    availableUsertags,
                    tag,
                    modifier = modifier,
                ) {
                    tags[idx] = it
                }
            }
        }
    }

    return {
        rule.share = shareVal.value
        rule.name = name.text
        rule.combineWithAnd = combineWithAnd

        rule.getTags().toSet().forEach { rule.removeTag(it) }// toSet() is necessary to prevent modifying the list which is currently iterated
        tags.forEach { rule.addTag(it) }
    }
}
