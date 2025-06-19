package apps.chocolatecakecodes.bluebeats.mpv.editor.media

import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaDir
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries

internal class MediaLibrary(
    rootPath: String,
) {

    val rootDir: MediaDir = scanDir(Path(rootPath))

    private fun scanDir(path: Path): MediaDir {
        val (dirPaths, filePaths) = path.listDirectoryEntries().partition {
            it.isDirectory()
        }
        val files = filePaths.map {
            SimpleMediaFile(it.absolutePathString())
        }
        val dirs = dirPaths.map {
            scanDir(it)
        }
        return SimpleMediaDir(path.absolutePathString(), dirs, files)
    }
}
