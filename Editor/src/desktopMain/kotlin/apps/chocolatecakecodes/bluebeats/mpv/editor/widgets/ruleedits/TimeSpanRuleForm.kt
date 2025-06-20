package apps.chocolatecakecodes.bluebeats.mpv.editor.widgets.ruleedits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import apps.chocolatecakecodes.bluebeats.blueplaylists.model.tag.Chapter
import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.TimeSpanRule
import apps.chocolatecakecodes.bluebeats.mpv.editor.LoadedFile
import apps.chocolatecakecodes.bluebeats.mpv.editor.media.FsTools
import apps.chocolatecakecodes.bluebeats.mpv.editor.media.SimpleMediaFile
import apps.chocolatecakecodes.bluebeats.mpv.editor.utils.observeStateChange
import apps.chocolatecakecodes.bluebeats.mpv.editor.widgets.LabeledCheckbox
import apps.chocolatecakecodes.bluebeats.mpv.editor.widgets.ShareForm
import apps.chocolatecakecodes.bluebeats.mpv.editor.widgets.SimpleDropdownSelectN
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlin.io.path.Path
import kotlin.io.path.pathString

/**
 * @return callback to be called when changes should be applied to LoadedFile
 */
@Composable
internal fun TimeSpanRuleForm(rule: TimeSpanRule, negated: MutableState<Boolean>): () -> Unit {
    val fsTools = remember { FsTools(LoadedFile.mediaLib.rootDir) }
    val share = remember { mutableStateOf(rule.share) }
    val shareVal = rememberUpdatedState(share.value)
    var name by remember { mutableStateOf(TextFieldValue(rule.name)) }
    var description by remember { mutableStateOf(TextFieldValue(rule.description)) }
    val file = remember { mutableStateOf(fsTools.mediaNodeToText(rule.file)) }
    val startMs = remember { mutableStateOf(rule.startMs) }
    val endMs = remember { mutableStateOf(rule.endMs) }
    var availableChapters by remember { mutableStateOf(LoadedFile.mediaLib.chaptersOfFile(rule.file.path)) }
    val selectedChapter = remember { mutableStateOf<Chapter?>(null) }

    val filePicker = rememberFilePickerLauncher(
        title = "Select File",
        directory = PlatformFile(Path(file.value).pathString)
    ) {
        if(it == null) return@rememberFilePickerLauncher
        file.value = fsTools.mediaNodeToText(SimpleMediaFile(it.absolutePath()))
    }

    observeStateChange(file) { old, new ->
        if(old == new) return@observeStateChange
        description = TextFieldValue("")
        startMs.value = 0
        endMs.value = 1
        availableChapters = LoadedFile.mediaLib.chaptersOfFile(fsTools.textToMediaFile(new).path)
        selectedChapter.value = null
    }
    observeStateChange(selectedChapter) { old, new ->
        if(new == null || old == new) return@observeStateChange
        description = TextFieldValue(new.name)
        startMs.value = new.start
        endMs.value = new.end
    }

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

        Row {
            TextField(
                value = file.value,
                onValueChange = { file.value = it },
                label = { Text("File") },
                singleLine = true,
                modifier = Modifier.padding(end = 8.dp).weight(1f)
            )
            IconButton(
                onClick = { filePicker.launch() }
            ) {
                Icon(imageVector = Icons.Default.FileOpen, contentDescription = "Browse")
            }
        }

        TextField(
            description,
            onValueChange = { description = it },
            label = { Text("Description") },
            singleLine = true,
        )

        NumberMsField(startMs, "Start (ms)")
        NumberMsField(endMs, "End (ms)")

        SimpleDropdownSelectN(
            "Apply Chapter",
            availableChapters,
            selectedChapter,
            enabled = availableChapters.isNotEmpty(),
            itemTitle = { it.name }
        )
    }

    return {
        rule.share = shareVal.value
        rule.name = name.text
        rule.description = description.text
        rule.startMs = startMs.value
        rule.endMs = endMs.value
        rule.file = fsTools.textToMediaFile(file.value)
    }
}

@Composable
private fun NumberMsField(value: MutableState<Long>, label: String) {
    var invalid by remember { mutableStateOf(false) }
    TextField(
        value.value.toString(),
        label = { Text(label) },
        singleLine = true,
        isError = invalid,
        onValueChange = { text: String ->
            val num = text.toLongOrNull()
            if(num != null && num > 0) {
                value.value = num
                invalid = false
            } else {
                invalid = true
            }
        }
    )
}
