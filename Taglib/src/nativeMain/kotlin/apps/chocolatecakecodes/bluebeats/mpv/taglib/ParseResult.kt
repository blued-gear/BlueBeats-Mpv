package apps.chocolatecakecodes.bluebeats.mpv.taglib

import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaFile
import apps.chocolatecakecodes.bluebeats.blueplaylists.model.tag.Chapter
import apps.chocolatecakecodes.bluebeats.blueplaylists.model.tag.TagFields
import apps.chocolatecakecodes.bluebeats.blueplaylists.model.tag.UserTags

data class ParseResult(
    val file: String,
    val type: MediaFile.Type,
    val tags: TagFields,
    val chapters: List<Chapter>?,
    val usertags: UserTags,
)
