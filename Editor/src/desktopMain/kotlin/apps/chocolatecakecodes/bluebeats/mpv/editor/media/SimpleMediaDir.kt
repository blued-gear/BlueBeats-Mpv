package apps.chocolatecakecodes.bluebeats.mpv.editor.media

import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaDir
import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaFile
import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaNode
import kotlin.io.path.Path
import kotlin.io.path.name

internal class SimpleMediaDir(
    override val path: String,
    private val dirs: List<MediaDir> = emptyList(),
    private val files: List<MediaFile> = emptyList(),
) : MediaDir() {

    override val name: String = Path(path).name
    override val id: Long = UNSPECIFIED_NODE_ID
    override val parent: MediaNode? = null

    override fun getDirs(): List<MediaDir> = dirs

    override fun getFiles(): List<MediaFile> = files

    override fun findChild(name: String): MediaNode? = null

    override fun createCopy(): MediaDir {
        return this
    }

    override fun deepEquals(that: MediaDir?): Boolean {
        return this === that
    }

    override fun equals(that: Any?): Boolean {
        if(that !is SimpleMediaDir)
            return false
        return this.path === that.path
    }

    override fun hashCode(): Int {
        return arrayOf(SimpleMediaDir::class.qualifiedName, path).contentHashCode()
    }

    override fun toString(): String {
        return "SimpleMediaDir: $path"
    }
}
