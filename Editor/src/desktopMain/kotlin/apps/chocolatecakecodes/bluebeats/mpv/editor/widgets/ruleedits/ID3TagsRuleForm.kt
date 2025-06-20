package apps.chocolatecakecodes.bluebeats.mpv.editor.widgets.ruleedits

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.ID3TagsRule
import apps.chocolatecakecodes.bluebeats.mpv.editor.LoadedFile
import apps.chocolatecakecodes.bluebeats.mpv.editor.widgets.*
import apps.chocolatecakecodes.bluebeats.mpv.serialization.misc.ID3TagType

/**
 * @return callback to be called when changes should be applied to LoadedFile
 */
@Composable
internal fun ID3TagsRuleForm(rule: ID3TagsRule, negated: MutableState<Boolean>): () -> Unit {
    val share = remember { mutableStateOf(rule.share) }
    val shareVal = rememberUpdatedState(share.value)
    var name by remember { mutableStateOf(TextFieldValue(rule.name)) }
    val tagType = remember { mutableStateOf(ID3TagType.valueOf(rule.tagType)) }
    val tagValues = remember { rule.getTagValues().toMutableStateList() }
    val availableTags by remember { derivedStateOf { LoadedFile.mediaLib.existingId3Tags[tagType.value]!!.sorted() } }

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
            modifier = Modifier.fillMaxWidth(),
        )

        SimpleDropdownSelect(
            "Tag type",
            ID3TagType.entries.minus(ID3TagType.INVALID),
            tagType,
            itemTitle = { it.description },
            modifier = Modifier.fillMaxWidth(),
        )

        Text("Values:")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.onSurface)
                .padding(all = 8.dp)
        ) {
            ModifiableWidgetList(
                tagValues,
                { "" }
            ) { tag, idx, modifier ->
                SearchableDropdownEdit(
                    "",
                    availableTags,
                    tag,
                    modifier = modifier,
                ) {
                    tagValues[idx] = it
                }
            }
        }
    }

    return {
        rule.share = shareVal.value
        rule.name = name.text
        rule.tagType = tagType.value.name

        rule.getTagValues().toSet().forEach { rule.removeTagValue(it) }// toSet() is necessary to prevent modifying the list which is currently iterated
        tagValues.forEach { rule.addTagValue(it) }
    }
}
