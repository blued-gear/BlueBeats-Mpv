package apps.chocolatecakecodes.bluebeats.mpv.editor.media

import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaDir
import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaFile
import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaNode
import apps.chocolatecakecodes.bluebeats.mpv.serialization.FsTools
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.isDirectory
import kotlin.io.path.pathString

internal class FsTools(rootDir: MediaDir) : FsTools(rootDir) {

    // if we would keep the original restriction, that only existing files can be resolved
    //  then the editor would be unable to display IncludeRule and similar when used without a fitting media-root
    override fun resolvePath(path: String): MediaNode? {
        val resolvedPath = Path(this.rootPath, path).absolute().normalize()
        return if(path.endsWith('/') || resolvedPath.isDirectory())
            SimpleMediaDir(resolvedPath.pathString)
        else
            SimpleMediaFile(resolvedPath.pathString)
    }

    fun mediaNodeToText(node: MediaNode): String {
        return "./" + relativizePath(node)
    }

    fun textToMediaDir(text: String): MediaDir {
        return SimpleMediaDir(Path(rootPath, text).absolute().normalize().pathString)
    }

    fun textToMediaFile(text: String): MediaFile {
        return SimpleMediaFile(Path(rootPath, text).absolute().normalize().pathString)
    }
}
