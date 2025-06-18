package apps.chocolatecakecodes.bluebeats.mpv.editor.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.Share

@Composable
internal fun ShareForm(share: MutableState<Share>, enabled: Boolean = true) {
    Column {
        Row(
            modifier = Modifier.padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LabeledRadioButton("Relative", share.value.modeRelative(), enabled) { share.value = Share.relative() }
            LabeledRadioButton("Absolute", share.value.modeAbsolute(), enabled) { share.value = Share.absolute() }
            LabeledRadioButton("Even", share.value.modeEven(), enabled) { share.value = Share.even() }
            LabeledRadioButton("Unlimited", share.value.modeUnlimited(), enabled) { share.value = Share.unlimited() }
        }

        val shareValueInvalid = remember { mutableStateOf(false) }
        TextField(
            share.value.value.toString(),
            label = { Text("Value") },
            enabled = share.value.let { enabled && (it.modeRelative() || it.modeAbsolute()) },
            singleLine = true,
            isError = shareValueInvalid.value,
            onValueChange = { text: String ->
                val num = text.toFloatOrNull()
                if(num != null && num > 0) {
                    share.value = share.value.copy(value = num)
                    shareValueInvalid.value = false
                } else {
                    shareValueInvalid.value = true
                }
            }
        )
    }
}
