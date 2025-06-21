package apps.chocolatecakecodes.bluebeats.mpv.editor.media

import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaDir
import apps.chocolatecakecodes.bluebeats.blueplaylists.model.tag.Chapter
import apps.chocolatecakecodes.bluebeats.blueplaylists.model.tag.TagFields
import apps.chocolatecakecodes.bluebeats.blueplaylists.model.tag.UserTags
import apps.chocolatecakecodes.bluebeats.mpv.editor.utils.Logger
import apps.chocolatecakecodes.bluebeats.mpv.serialization.misc.ID3TagType
import com.mpatric.mp3agic.*
import java.nio.file.Path
import java.util.*
import kotlin.io.path.*

internal class MediaLibrary(
    rootPath: String,
) {

    val rootDir: MediaDir

    private val existingId3TagsMut: EnumMap<ID3TagType, MutableSet<String>> = EnumMap(ID3TagType::class.java)
    @Suppress("UNCHECKED_CAST")
    val existingId3Tags: EnumMap<ID3TagType, Set<String>> = existingId3TagsMut as EnumMap<ID3TagType, Set<String>>
    private val existingUsertagsMut: MutableSet<String> = mutableSetOf()
    val existingUsertags: Set<String> = existingUsertagsMut

    init {
        existingId3TagsMut[ID3TagType.TITLE] = mutableSetOf()
        existingId3TagsMut[ID3TagType.ARTIST] = mutableSetOf()
        existingId3TagsMut[ID3TagType.GENRE] = mutableSetOf()

        rootDir = scanDir(Path(rootPath))
    }

    private fun scanDir(path: Path): MediaDir {
        val (dirPaths, filePaths) = path.listDirectoryEntries().partition {
            it.isDirectory()
        }

        val files = filePaths.map {
            SimpleMediaFile(it.absolutePathString())
        }
        val dirs = dirPaths.map {
            scanDir(it)
        }

        files.forEach { parseFile(it.path) }

        return SimpleMediaDir(path.absolutePathString(), dirs, files)
    }

    fun chaptersOfFile(path: String): List<Chapter> {
        if(!Path(path).exists()) return emptyList()

        try {
            val parser = Mp3File(path)
            if(parser.hasId3v2Tag()) {
                return parser.id3v2Tag.chapters?.map {
                    val title = it.subframes.first { it.id == AbstractID3v2Tag.ID_TITLE }?.let {
                        ID3v2TextFrameData(it.hasUnsynchronisation(), it.data).text.toString()
                    } ?: it.id

                    Chapter(
                        it.startTime.toLong(),
                        it.endTime.toLong(),
                        title
                    )
                } ?: emptyList()
            }
        } catch(e: Exception) {
            Logger.error("MediaLibrary::chaptersOfFile", "unable to read chapters ($path)", e)
        }

        return emptyList()
    }

    private fun parseFile(path: String) {
        try {
            val parser = Mp3File(path)
            readStandardTags(parser).let { tags ->
                tags.title?.let { existingId3TagsMut.get(ID3TagType.TITLE)!!.add(it) }
                tags.artist?.let { existingId3TagsMut.get(ID3TagType.ARTIST)!!.add(it) }
                tags.genre?.let { existingId3TagsMut.get(ID3TagType.GENRE)!!.add(it) }
            }
            readUsertags(parser).let { tags ->
                existingUsertagsMut.addAll(tags)
            }
        } catch(e: Exception) {
            Logger.warn("MediaLibrary::parseFile", "unable to read parse file ($path)", e, false)
        }
    }

    private fun readStandardTags(parser: Mp3File): TagFields {
        var title: String? = null
        var artist: String? = null
        var genre: String? = null

        if(parser.hasId3v1Tag()) {
            val data = parser.id3v1Tag
            title = data.title
            artist = data.artist
            genre = data.genreDescription
        }

        if(parser.hasId3v2Tag()) {
            val data = parser.id3v2Tag
            title = data.title
            artist = data.artist
            genre = data.genreDescription
        }

        val length = parser.lengthInMilliseconds

        return TagFields(
            title = title,
            artist = artist,
            genre = genre,
            length = length
        )
    }

    private fun readUsertags(parser: Mp3File): List<String> {
        if(parser.hasId3v2Tag()) {
            parser.id3v2Tag.frameSets["TXXX"]?.frames?.forEach { frame ->
                try {
                    val (desc, text) = decodeTXXXFrame(frame)
                    if(desc == UserTags.Parser.USERTEXT_KEY) {
                        return UserTags.Parser.parse(text).tags
                    }
                } catch (e: Exception) {
                    Logger.warn("MediaLibrary::readUsertags", "unable to decode TXXX frame (file: ${parser.filename})", e, false)
                }
            }
        }
        return emptyList()
    }

    // code adapted from ID3v2UrlFrameData
    private fun decodeTXXXFrame(frame: ID3v2Frame): Pair<String, String> {
        val bytes = if(frame.hasUnsynchronisation()) BufferTools.synchroniseBuffer(frame.data) else frame.data

        val description: EncodedText
        val text: EncodedText

        var marker = BufferTools.indexOfTerminatorForEncoding(bytes, 1, bytes[0].toInt())
        if (marker >= 0) {
            description = EncodedText(bytes[0], BufferTools.copyBuffer(bytes, 1, marker - 1))
            marker += description.terminator.size
        } else {
            description = EncodedText(bytes[0], "")
            marker = 1
        }

        text = EncodedText(bytes[0], BufferTools.copyBuffer(bytes, marker, bytes.size - marker))

        @Suppress("USELESS_ELVIS")
        return Pair(
            description.toString() ?: throw InvalidDataException("TXXX description could not be decoded"),
            text.toString() ?: throw InvalidDataException("TXXX text could not be decoded")
        )
    }
}
