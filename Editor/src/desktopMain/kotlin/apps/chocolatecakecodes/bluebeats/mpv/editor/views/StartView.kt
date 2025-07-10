package apps.chocolatecakecodes.bluebeats.mpv.editor.views

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import apps.chocolatecakecodes.bluebeats.mpv.editor.LoadedFile
import apps.chocolatecakecodes.bluebeats.mpv.editor.utils.Logger
import com.dokar.sonner.*
import io.github.vinceglb.filekit.absoluteFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.PickerResultLauncher
import io.github.vinceglb.filekit.dialogs.compose.SaverResultLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun StartView(onContinue: () -> Unit) {
    val toaster = rememberToasterState()
    val loading = remember { mutableStateOf(false) }
    val onClickNewHandler = onClickNew(loading, onContinue)
    val onClickOpenHandler = onClickOpen(loading, toaster, onContinue)

    Toaster(toaster, alignment = Alignment.TopEnd)

    Column(
        modifier = Modifier.fillMaxSize().safeContentPadding(),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Button(
                enabled = !loading.value,
                onClick = { onClickNewHandler.launch("playlist", "bbdp") }
            ) {
                Text("New File")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                enabled = !loading.value,
                onClick = { onClickOpenHandler.launch() }
            ) {
                Text("Open File")
            }
        }

        if(loading.value) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text("Loading...")
            }
        }
    }
}

@Composable
private fun onClickNew(loading: MutableState<Boolean>, onContinue: () -> Unit): SaverResultLauncher {
    return rememberFileSaverLauncher { file ->
        if(file == null) return@rememberFileSaverLauncher

        loading.value = true
        LoadedFile.initEmpty(file.absolutePath())
        onContinue()
    }
}

@Composable
private fun onClickOpen(loading: MutableState<Boolean>, toaster: ToasterState, onContinue: () -> Unit): PickerResultLauncher {
    return rememberFilePickerLauncher(
        title = "Open DynPl File",
        mode = FileKitMode.Single,
        type = FileKitType.File("bbdp", "json"),
    ) { file ->
        if(file == null) return@rememberFilePickerLauncher

        loading.value = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                LoadedFile.load(file.absoluteFile().path)

                withContext(Dispatchers.Main) {
                    onContinue()
                }
            } catch(e: Exception) {
                Logger.error("StartView", "exception in LoadedFile::load", e)

                withContext(Dispatchers.Main) {
                    toaster.show(
                        "Unable to load file\n${e.localizedMessage}",
                        type = ToastType.Error, duration = ToasterDefaults.DurationLong
                    )
                }
            }
        }
    }
}
