package apps.chocolatecakecodes.bluebeats.mpv.editor.views

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import apps.chocolatecakecodes.bluebeats.mpv.editor.LoadedFile
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

@Composable
internal fun StartView(onContinue: () -> Unit) {
    val toaster = rememberToasterState()
    val onClickNewHandler = onClickNew(onContinue)
    val onClickOpenHandler = onClickOpen(toaster, onContinue)

    Toaster(toaster, alignment = Alignment.TopEnd)

    Row(
        modifier = Modifier.safeContentPadding().fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = { onClickNewHandler.launch("playlist", "bbdp") }) {
            Text("New File")
        }
        Spacer(modifier = Modifier.width(16.dp))
        Button(onClick = { onClickOpenHandler.launch() }) {
            Text("Open File")
        }
    }
}

@Composable
private fun onClickNew(onContinue: () -> Unit): SaverResultLauncher {
    return rememberFileSaverLauncher { file ->
        if(file == null) return@rememberFileSaverLauncher

        LoadedFile.initEmpty(file.absolutePath())
        onContinue()
    }
}

@Composable
private fun onClickOpen(toaster: ToasterState, onContinue: () -> Unit): PickerResultLauncher {
    return rememberFilePickerLauncher(
        title = "Open DynPl File",
        mode = FileKitMode.Single,
        type = FileKitType.File("bbdp", "json"),
    ) { file ->
        if(file == null) return@rememberFilePickerLauncher

        try {
            LoadedFile.load(file.absoluteFile().path)
            onContinue()
        } catch(e: Exception) {
            e.printStackTrace()
            toaster.show("Unable to load file\n${e.localizedMessage}",
                type = ToastType.Error, duration = ToasterDefaults.DurationLong)
        }
    }
}
