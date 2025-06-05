package apps.chocolatecakecodes.bluebeats.mpv.media

import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaFile
import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaNode
import apps.chocolatecakecodes.bluebeats.blueplaylists.model.tag.Chapter
import apps.chocolatecakecodes.bluebeats.blueplaylists.model.tag.TagFields
import kotlinx.io.files.Path

internal class MediaFileImpl(
    path: Path,
    override val type: Type,
    override val parent: MediaNode?,
) : MediaFile() {

    companion object {
        private val EMPTY_TAGS = TagFields()
        private val EMPTY_USERTAGS = emptyList<String>()
    }

    override var mediaTags: TagFields = EMPTY_TAGS
    override var chapters: List<Chapter>? = null
    override var userTags: List<String> = EMPTY_USERTAGS

    override val id: Long = path.hashCode().toLong()
    override val name: String = path.name
    override val path: String = path.toString()

    override fun toString(): String {
        return "MediaFile: $path"
    }

    override fun hashCode(): Int {
        return arrayOf(this::class.qualifiedName!!, path).contentHashCode()
    }

    override fun equals(that: Any?): Boolean {
        if(that !is MediaFile)
            return false
        if(!shallowEquals(that))
            return false

        if(that.chapters != this.chapters
            || that.mediaTags != this.mediaTags
            || that.userTags != this.userTags)
            return false

        return true
    }

    override fun shallowEquals(that: MediaFile?): Boolean {
        if(that === null)
            return false
        return this.type == that.type && this.path == that.path
    }
}
