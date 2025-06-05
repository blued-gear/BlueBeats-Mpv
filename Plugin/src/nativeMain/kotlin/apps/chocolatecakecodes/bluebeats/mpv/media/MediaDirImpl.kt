package apps.chocolatecakecodes.bluebeats.mpv.media

import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaDir
import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaFile
import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaNode
import kotlinx.io.files.Path

internal class MediaDirImpl(
    path: Path,
    parent: MediaDir? = null,
) : MediaDir() {

    private val dirs = mutableListOf<MediaDir>()
    private val files = mutableListOf<MediaFile>()

    override val id: Long = path.hashCode().toLong()
    override val name: String = path.name
    override val path: String = path.toString()
    override val parent: MediaNode? = parent

    fun addDir(dir: MediaDir) {
        if(this !== dir.parent)
            throw IllegalArgumentException("dir is not sub-item of this dir")
        dirs.add(dir)
    }

    fun addFile(file: MediaFile) {
        if(this !== file.parent)
            throw IllegalArgumentException("file is not sub-item of this dir")
        files.add(file)
    }

    override fun getDirs(): List<MediaDir> {
        return dirs
    }

    override fun getFiles(): List<MediaFile> {
        return files
    }

    override fun findChild(name: String): MediaNode? {
        return files.firstOrNull { it.name == name }
            ?: dirs.firstOrNull { it.name == name }
    }

    override fun createCopy(): MediaDir {
        return MediaDirImpl(
            Path(path),
            parent as MediaDir?
        ).also {
            it.dirs.addAll(dirs)
            it.files.addAll(files)
        }
    }

    override fun equals(that: Any?): Boolean {
        TODO("Not yet implemented")
    }

    override fun hashCode(): Int {
        return arrayOf(this::class.qualifiedName!!, path).contentHashCode()
    }

    override fun toString(): String {
        return "MediaDir: $path"
    }

    override fun deepEquals(that: MediaDir?): Boolean {
        if(that === null)
            return false
        if(this === that)
            return true

        val thisChildren = this.getDirs()
        val otherChildren = that.getDirs()
        if(thisChildren.size != otherChildren.size)
            return false
        for(i in thisChildren.indices)
            if(thisChildren[i] != otherChildren[i])
                return false

        val thisFiles = this.getFiles()
        val otherFiles = that.getFiles()
        if(thisFiles.size != otherFiles.size)
            return false
        for(i in thisFiles.indices){
            if(thisFiles[i] != otherFiles[i])
                return false
        }

        return true
    }
}
