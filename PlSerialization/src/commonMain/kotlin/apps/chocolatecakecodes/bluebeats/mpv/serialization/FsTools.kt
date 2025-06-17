package apps.chocolatecakecodes.bluebeats.mpv.serialization

import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaDir
import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaNode
import apps.chocolatecakecodes.bluebeats.blueplaylists.utils.castToOrNull

class FsTools(
    private val rootDir: MediaDir,
) {

    fun resolvePath(path: String): MediaNode? {
        return path.split('/').fold(rootDir as MediaNode?) { dir, name ->
            dir?.castToOrNull<MediaDir>()?.let {
                it.getDirs().find { it.name == name }
                    ?: it.getFiles().find { it.name == name }
            }
        }
    }

    fun relativizePath(node: MediaNode): String {
        return node.path.removePrefix(rootDir.path).removePrefix("/")
    }
}
