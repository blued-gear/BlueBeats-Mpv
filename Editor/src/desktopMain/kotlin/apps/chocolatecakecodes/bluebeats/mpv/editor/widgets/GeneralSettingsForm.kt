package apps.chocolatecakecodes.bluebeats.mpv.editor.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import apps.chocolatecakecodes.bluebeats.mpv.editor.LoadedFile

/**
 * @return callback to be called when changes should be applied to LoadedFile
 */
@Composable
internal fun GeneralSettingsForm(): () -> Unit {
    var mediaRoot by remember { mutableStateOf(TextFieldValue(LoadedFile.pl.mediaRoot)) }
    val iterationSize = remember { mutableStateOf(LoadedFile.pl.iterationSize) }
    val iterationSizeValid = remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextField(
            value = mediaRoot,
            onValueChange = { mediaRoot = it },
            label = { Text("Media root path") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        TextField(
            iterationSize.value.toString(),
            label = { Text("Batch size") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = iterationSizeValid.value,
            onValueChange = { text: String ->
                val num = text.toIntOrNull()
                if(num != null && num > 0) {
                    iterationSize.value = num
                    iterationSizeValid.value = false
                } else {
                    iterationSizeValid.value = true
                }
            }
        )
    }

    return {
        val rootPath = mediaRoot.text
        if(rootPath != LoadedFile.pl.mediaRoot)
            LoadedFile.setMediaRootPath(rootPath)
        LoadedFile.updatePl { it.copy(iterationSize = iterationSize.value) }
    }
}
