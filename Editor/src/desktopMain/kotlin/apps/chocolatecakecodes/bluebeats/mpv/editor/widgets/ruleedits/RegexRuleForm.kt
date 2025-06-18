package apps.chocolatecakecodes.bluebeats.mpv.editor.widgets.ruleedits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import apps.chocolatecakecodes.bluebeats.mpv.editor.widgets.ShareForm
import apps.chocolatecakecodes.bluebeats.mpv.editor.widgets.SimpleDropdownSelect
import java.util.regex.PatternSyntaxException

@Composable
internal fun RegexRuleForm(rule: RegexRule): () -> Unit {
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
        HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))

        TextField(
            name,
            onValueChange = { name = it },
            label = { Text("Name") },
            singleLine = true,
        )

        SimpleDropdownSelect(
            "Attribute",
            RegexRule.Attribute.entries.toSet(),
            attribute,
            itemTitle = { it.name.lowercase() }
        )

        TextField(
            regex.value,
            label = { Text("Regex") },
            singleLine = true,
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
