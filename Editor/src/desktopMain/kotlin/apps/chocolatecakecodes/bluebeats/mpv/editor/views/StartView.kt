package apps.chocolatecakecodes.bluebeats.mpv.editor.views

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaNode
import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.Rule
import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.RuleGroup
import apps.chocolatecakecodes.bluebeats.mpv.editor.LoadedFile
import apps.chocolatecakecodes.bluebeats.mpv.editor.utils.SimpleMediaDir
import apps.chocolatecakecodes.bluebeats.mpv.serialization.DynPl
import apps.chocolatecakecodes.bluebeats.mpv.serialization.FsTools
import apps.chocolatecakecodes.bluebeats.mpv.serialization.Serializer
import apps.chocolatecakecodes.bluebeats.mpv.serialization.rules.RuleGroupSerializable
import io.github.vinceglb.filekit.absoluteFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.PickerResultLauncher
import io.github.vinceglb.filekit.dialogs.compose.SaverResultLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.path
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import java.io.FileInputStream

@Composable
internal fun StartView(onContinue: () -> Unit) {
    val onClickNewHandler = onClickNew(onContinue)
    val onClickOpenHandler = onClickOpen(onContinue)

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

        val rootGroup = RuleGroup(
            1,
            true,
            Rule.Share(-1f, true),
            false
        )

        LoadedFile.filePath = file.absoluteFile().path
        LoadedFile.rootGroup = rootGroup
        LoadedFile.pl = DynPl(
            "",
            50,
            RuleGroupSerializable(rootGroup, FsTools(MediaNode.UNSPECIFIED_DIR))
        )

        onContinue()
    }
}

@Composable
@OptIn(ExperimentalSerializationApi::class)
private fun onClickOpen(onContinue: () -> Unit): PickerResultLauncher {
    return rememberFilePickerLauncher(
        title = "Open DynPl File",
        mode = FileKitMode.Single,
        type = FileKitType.File("bbdp", "json"),
    ) { file ->
        if(file == null) return@rememberFilePickerLauncher

        try {
            FileInputStream(file.file).use { inp ->
                val pl = Serializer.json.decodeFromStream<DynPl>(inp)
                LoadedFile.filePath = file.absoluteFile().path
                LoadedFile.pl = pl
                LoadedFile.rootGroup = pl.rootRule.unpack(FsTools(SimpleMediaDir(pl.mediaRoot)))

                onContinue()
            }
        } catch(e: Exception) {
            e.printStackTrace()
            //TODO show in UI
        }
    }
}
