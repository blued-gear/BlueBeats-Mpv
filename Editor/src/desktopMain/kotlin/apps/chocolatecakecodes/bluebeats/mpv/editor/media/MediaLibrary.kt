package apps.chocolatecakecodes.bluebeats.mpv.editor.media

import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaDir
import apps.chocolatecakecodes.bluebeats.blueplaylists.model.tag.Chapter
import com.mpatric.mp3agic.AbstractID3v2Tag
import com.mpatric.mp3agic.ID3v2TextFrameData
import com.mpatric.mp3agic.Mp3File
import java.nio.file.Path
import kotlin.io.path.*

internal class MediaLibrary(
    rootPath: String,
) {

    val rootDir: MediaDir = scanDir(Path(rootPath))

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
        return SimpleMediaDir(path.absolutePathString(), dirs, files)
    }

    fun chaptersOfFile(path: String): List<Chapter> {
        if(!Path(path).exists()) return emptyList()

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
        return emptyList()
    }
}
