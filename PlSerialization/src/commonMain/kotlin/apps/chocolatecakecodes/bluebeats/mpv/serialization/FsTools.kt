package apps.chocolatecakecodes.bluebeats.mpv.serialization

import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaDir
import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaNode
import apps.chocolatecakecodes.bluebeats.blueplaylists.utils.castToOrNull

class FsTools(
    private val rootDir: MediaDir,
) {

    private val rootPath = rootDir.path.let { if(it.endsWith('/')) it else "$it/" }

    fun resolvePath(path: String): MediaNode? {
        return path.split('/').fold(rootDir as MediaNode?) { dir, name ->
            dir?.castToOrNull<MediaDir>()?.let {
                it.getDirs().find { it.name == name }
                    ?: it.getFiles().find { it.name == name }
            }
        }
    }

    fun relativizePath(node: MediaNode): String {
        val path = if(node is MediaDir) node.path.let { if(it.endsWith('/')) it else "$it/" } else node.path
        return path.removePrefix(rootPath).removePrefix("/")
    }
}
