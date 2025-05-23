package apps.chocolatecakecodes.bluebeats.mpv.media

import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaFile
import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaNode
import apps.chocolatecakecodes.bluebeats.blueplaylists.model.tag.Chapter
import apps.chocolatecakecodes.bluebeats.blueplaylists.model.tag.TagFields

internal class MediaFileImpl : MediaFile() {
    override val type: Type
        get() = TODO("Not yet implemented")
    override var mediaTags: TagFields
        get() = TODO("Not yet implemented")
        set(value) {}
    override var chapters: List<Chapter>?
        get() = TODO("Not yet implemented")
        set(value) {}
    override var userTags: List<String>
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun shallowEquals(that: MediaFile?): Boolean {
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
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        TODO("Not yet implemented")
    }

}
