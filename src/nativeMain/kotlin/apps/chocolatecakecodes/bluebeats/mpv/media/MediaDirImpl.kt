package apps.chocolatecakecodes.bluebeats.mpv.media

import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaDir
import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaFile
import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaNode

internal class MediaDirImpl : MediaDir() {
    override fun getDirs(): List<MediaDir> {
        TODO("Not yet implemented")
    }

    override fun getFiles(): List<MediaFile> {
        TODO("Not yet implemented")
    }

    override fun findChild(name: String): MediaNode? {
        TODO("Not yet implemented")
    }

    override fun createCopy(): MediaDir {
        TODO("Not yet implemented")
    }

    override fun deepEquals(that: MediaDir?): Boolean {
        TODO("Not yet implemented")
    }

    override val id: Long
        get() = TODO("Not yet implemented")
    override val name: String
        get() = TODO("Not yet implemented")
    override val path: String
        get() = TODO("Not yet implemented")
    override val parent: MediaNode?
        get() = TODO("Not yet implemented")

    override fun equals(that: Any?): Boolean {
        TODO("Not yet implemented")
    }

    override fun hashCode(): Int {
        return -1
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        TODO("Not yet implemented")
    }
}
