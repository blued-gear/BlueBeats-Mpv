package apps.chocolatecakecodes.bluebeats.mpv.editor.widgets.ruleedits

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
import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.RegexRule
import apps.chocolatecakecodes.bluebeats.mpv.editor.widgets.LabeledCheckbox
import apps.chocolatecakecodes.bluebeats.mpv.editor.widgets.ShareForm
import apps.chocolatecakecodes.bluebeats.mpv.editor.widgets.SimpleDropdownSelect
import java.util.regex.PatternSyntaxException

/**
 * @return callback to be called when changes should be applied to LoadedFile
 */
@Composable
internal fun RegexRuleForm(rule: RegexRule, negated: MutableState<Boolean>): () -> Unit {
    val share = remember { mutableStateOf(rule.share) }
    val shareVal = rememberUpdatedState(share.value)
    var name by remember { mutableStateOf(TextFieldValue(rule.name)) }
    val attribute = remember { mutableStateOf(rule.attribute) }
    val regex = remember { mutableStateOf(rule.regex) }
    var regexErr by remember { mutableStateOf<String?>(null) }

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
            "Attribute",
            RegexRule.Attribute.entries,
            attribute,
            itemTitle = { it.name.lowercase() },
            modifier = Modifier.fillMaxWidth(),
        )

        TextField(
            regex.value,
            label = { Text("Regex") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            onValueChange = {
                regex.value = it

                try {
                    Regex(it)
                    regexErr = null
                } catch (e: PatternSyntaxException) {
                    regexErr = e.description
                    return@TextField
                }
            },
        )
        regexErr?.let { err ->
            Text(err, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 8.dp))
        }
    }

    return {
        rule.share = shareVal.value
        rule.name = name.text
        rule.attribute = attribute.value

        if(regexErr == null)
            rule.regex = regex.value
    }
}
