package apps.chocolatecakecodes.bluebeats.mpv.editor.widgets.ruleedits

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaDir
import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaFile
import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaNode
import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.IncludeRule
import apps.chocolatecakecodes.bluebeats.mpv.editor.LoadedFile
import apps.chocolatecakecodes.bluebeats.mpv.editor.media.FsTools
import apps.chocolatecakecodes.bluebeats.mpv.editor.media.SimpleMediaDir
import apps.chocolatecakecodes.bluebeats.mpv.editor.media.SimpleMediaFile
import apps.chocolatecakecodes.bluebeats.mpv.editor.widgets.LabeledCheckbox
import apps.chocolatecakecodes.bluebeats.mpv.editor.widgets.ModifiableStringList
import apps.chocolatecakecodes.bluebeats.mpv.editor.widgets.ShareForm
import com.dokar.sonner.*
import io.github.vinceglb.filekit.*
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.dialogs.openFilePicker
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.pathString

@Composable
internal fun IncludeRuleForm(rule: IncludeRule): () -> Unit {
    val toaster = rememberToasterState()
    val share = remember { mutableStateOf(rule.share) }
    val shareVal = rememberUpdatedState(share.value)
    var name by remember { mutableStateOf(TextFieldValue(rule.name)) }
    val fsTools = remember { FsTools(LoadedFile.mediaLib.rootDir) }
    val dirs = remember { rule.getDirs().toMutableStateList() }
    val files = remember { rule.getFiles().map { "./" + fsTools.relativizePath(it) }.toMutableStateList() }
    val lastPickerDir = remember { mutableStateOf(PlatformFile(LoadedFile.pl.mediaRoot)) }

    Toaster(toaster, alignment = Alignment.TopEnd)

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

        Text("Directories:")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.onSurface)
                .padding(all = 8.dp)
        ) {
            DirList(dirs, fsTools, lastPickerDir, toaster)
        }

        Text("Files:")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.onSurface)
                .padding(all = 8.dp)
        ) {
            ModifiableStringList(files) {
                val file = FileKit.openFilePicker(
                    title = "Add File",
                    directory = lastPickerDir.value,
                    dialogSettings = FileKitDialogSettings.createDefault()
                ) ?: return@ModifiableStringList null

                if(!Path(file.path).absolute().startsWith(Path(LoadedFile.pl.mediaRoot))) {
                    toaster.show(
                        message = "File must have the Media-root as a parent",
                        type = ToastType.Error,
                        duration = ToasterDefaults.DurationLong
                    )
                    return@ModifiableStringList null
                }

                lastPickerDir.value = file.parent()!!
                return@ModifiableStringList mediaNodeToStr(SimpleMediaFile(file.path), fsTools)
            }
        }
    }

    return {
        rule.share = shareVal.value
        rule.name = name.text

        rule.getDirs().forEach { rule.removeDir(it.first) }
        dirs.forEach { rule.addDir(it.first, it.second) }

        // toSet() is necessary to prevent modifying the list which is currently iterated
        rule.getFiles().toSet().forEach { rule.removeFile(it) }
        files.map { strToMediaFile(it) }.forEach { rule.addFile(it) }
    }
}

@Composable
private fun DirList(dirs: SnapshotStateList<Pair<MediaDir, Boolean>>, fsTools: FsTools, lastPickerDir: MutableState<PlatformFile>, toaster: ToasterState) {
    val dirPicker = rememberDirectoryPickerLauncher(
        title = "Add Directory",
        directory = lastPickerDir.value,
        dialogSettings = FileKitDialogSettings.createDefault()
    ) { dir ->
        if(dir == null) return@rememberDirectoryPickerLauncher

        if(!Path(dir.path).absolute().startsWith(Path(LoadedFile.pl.mediaRoot))) {
            toaster.show(
                message = "Dir must have the Media-root as a parent",
                type = ToastType.Error,
                duration = ToasterDefaults.DurationLong
            )
            return@rememberDirectoryPickerLauncher
        }

        dir.parent()?.let { lastPickerDir.value = it }
        dirs.add(Pair(SimpleMediaDir(dir.absolutePath()), true))
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        dirs.forEachIndexed { idx, (dir, recursive) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = mediaNodeToStr(dir, fsTools),
                    onValueChange = { dirs[idx] = Pair(strToMediaDir(it), recursive) },
                    modifier = Modifier.padding(end = 12.dp).weight(1.0f),
                    singleLine = true,
                )
                LabeledCheckbox("recursive", recursive) {
                    dirs[idx] = Pair(dir, it)
                }
                IconButton(
                    onClick = { dirs.removeAt(idx) }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "remove item",
                    )
                }
            }
        }

        IconButton(
            modifier = Modifier.padding(top = 8.dp),
            onClick = { dirPicker.launch() }
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "add item",
            )
        }
    }
}

private fun mediaNodeToStr(node: MediaNode, fsTools: FsTools): String {
    return "./" + fsTools.relativizePath(node)
}

private fun strToMediaDir(str: String): MediaDir {
    return SimpleMediaDir(Path(LoadedFile.pl.mediaRoot, str).absolute().normalize().pathString)
}

private fun strToMediaFile(str: String): MediaFile {
    return SimpleMediaFile(Path(LoadedFile.pl.mediaRoot, str).absolute().normalize().pathString)
}
