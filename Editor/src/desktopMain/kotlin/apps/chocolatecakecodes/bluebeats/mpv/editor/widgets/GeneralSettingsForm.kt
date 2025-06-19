package apps.chocolatecakecodes.bluebeats.mpv.editor.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import apps.chocolatecakecodes.bluebeats.mpv.editor.LoadedFile

/**
 * @return callback to be called when changes should be applied to LoadedFile
 */
@Composable
internal fun GeneralSettingsForm(): () -> Unit {
    val mediaRoot = rememberTextFieldState(LoadedFile.pl.mediaRoot)
    val iterationSize = remember { mutableStateOf(LoadedFile.pl.iterationSize) }
    val iterationSizeValid = remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row {
            TextField(
                mediaRoot,
                label = { Text("Media root path") },
                lineLimits = TextFieldLineLimits.SingleLine,
            )
        }
        Row {
            TextField(
                iterationSize.value.toString(),
                label = { Text("Batch size") },
                singleLine = true,
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
    }

    return {
        LoadedFile.setMediaRootPath(mediaRoot.text.toString())
        LoadedFile.updatePl { it.copy(iterationSize = iterationSize.value) }
    }
}
