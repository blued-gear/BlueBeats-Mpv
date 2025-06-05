package apps.chocolatecakecodes.bluebeats.mpv.taglib

import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.log.Logger
import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaFile
import apps.chocolatecakecodes.bluebeats.blueplaylists.model.tag.Chapter
import apps.chocolatecakecodes.bluebeats.blueplaylists.model.tag.TagFields
import apps.chocolatecakecodes.bluebeats.blueplaylists.model.tag.UserTags
import kotlinx.cinterop.*
import taglib.*

@ExperimentalForeignApi
object TagParser {

    fun parseFile(filePath: String): ParseResult {
        val file: CPointer<TaglibFile>? = taglib_open_file(filePath)
        if(file == null)
            throw ParseException("failed to open file $filePath")

        try {
            val type = readType(file)
            val tags = readTags(file)
            val chapters = readChapters(file)
            val usertags = readUsertags(file)

            if(tags.length < 1) {
                Logger.Slot.INSTANCE.warn("TagParser", "unable to parse length of title ($filePath)")
            }

            return ParseResult(filePath, type, tags, chapters, usertags)
        } finally {
            taglib_close_file(file)
        }
    }

    private fun readType(file: CPointer<TaglibFile>): MediaFile.Type {
        val type = taglib_get_filetype(file)
        return when(type) {
            FileType.UNDETERMINED -> MediaFile.Type.OTHER
            FileType.AUDIO -> MediaFile.Type.AUDIO
            FileType.VIDEO -> MediaFile.Type.VIDEO
        }
    }

    private fun readTags(file: CPointer<TaglibFile>): TagFields {
        val length = taglib_get_length(file).let {
            if(it == -1)
                0
            else
                it.toLong()
        }

        return TagFields(
            title = consumeString(taglib_get_title(file)),
            artist = consumeString(taglib_get_artist(file)),
            genre = consumeString(taglib_get_genre(file)),
            length = length
        )
    }

    private fun readChapters(file: CPointer<TaglibFile>): List<Chapter>? {
        val chaptersPtr = taglib_get_chapters(file)
        if(chaptersPtr == null)
            return null
        val chapters = chaptersPtr.pointed

        val ret = mutableListOf<Chapter>()
        try {
            for(i in 0..<chapters.size.toInt()) {
                val start = chapters.startTimes!!.get(i)
                val end = chapters.endTimes!!.get(i)

                if(start == 0xFFFFFFFFU)
                    continue

                val title = chapters.titles!!.get(i)?.toKString() ?: "~~?~~"
                ret.add(Chapter(start.toLong(), end.toLong(), title))
            }
        } finally {
            taglib_free_chapter_list(chaptersPtr)
        }
        return ret
    }

    private fun readUsertags(file: CPointer<TaglibFile>): UserTags {
        val usertextsPtr = taglib_get_usertexts(file)
        if(usertextsPtr == null)
            return UserTags(emptyList())
        val usertexts = usertextsPtr.pointed

        try {
            for(i in 0..<usertexts.size.toInt()) {
                val key = usertexts.keys!!.get(i)!!.toKString()
                if(key == UserTags.Parser.USERTEXT_KEY) {
                    val value = usertexts.values!!.get(i)?.toKString()
                    if(value != null) {
                        return UserTags.Parser.parse(value)
                    } else {
                        Logger.Slot.INSTANCE.warn("TagParser", "encountered null value for Usertags TXXX")
                        return UserTags(emptyList())
                    }
                }
            }

            return UserTags(emptyList())
        } finally {
            taglib_free_usertext_list(usertextsPtr)
        }
    }

    private fun consumeString(cStr: CPointer<ByteVarOf<Byte>>?): String? {
        if(cStr == null) return null
        val str = cStr.toKString()
        taglib_free_str(cStr)
        return str
    }
}
