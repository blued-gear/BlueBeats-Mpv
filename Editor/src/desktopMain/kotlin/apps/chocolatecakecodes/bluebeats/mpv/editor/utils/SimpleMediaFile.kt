package apps.chocolatecakecodes.bluebeats.mpv.editor.utils

import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaFile
import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaNode
import apps.chocolatecakecodes.bluebeats.blueplaylists.model.tag.Chapter
import apps.chocolatecakecodes.bluebeats.blueplaylists.model.tag.TagFields
import kotlin.io.path.Path
import kotlin.io.path.name

internal class SimpleMediaFile(
    override val path: String
) : MediaFile() {

    override val name: String = Path(path).name
    override val id: Long = UNSPECIFIED_NODE_ID
    override val parent: MediaNode? = null
    override val type: Type = Type.OTHER
    override var mediaTags: TagFields = TagFields()
    override var chapters: List<Chapter>? = null
    override var userTags: List<String> = emptyList()

    override fun shallowEquals(that: MediaFile?): Boolean {
        return this == that
    }

    override fun equals(that: Any?): Boolean {
        if(that !is SimpleMediaFile)
            return false
        return this.path === that.path
    }

    override fun hashCode(): Int {
        return arrayOf(SimpleMediaFile::class.qualifiedName, path).contentHashCode()
    }

    override fun toString(): String {
        return "SimpleMediaFile: $path"
    }
}
