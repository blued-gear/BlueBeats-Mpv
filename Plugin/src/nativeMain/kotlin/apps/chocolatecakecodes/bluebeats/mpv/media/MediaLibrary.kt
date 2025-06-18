package apps.chocolatecakecodes.bluebeats.mpv.media

import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaDir
import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaFile
import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaLibrary
import apps.chocolatecakecodes.bluebeats.mpv.serialization.misc.ID3TagType
import apps.chocolatecakecodes.bluebeats.mpv.taglib.TagParser
import apps.chocolatecakecodes.bluebeats.mpv.utils.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.chunked
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

internal class MediaLibraryImpl(
    private val root: Path
) : MediaLibrary {

    companion object {
        private const val FILES_CHUNK_SIZE: Int = 32
    }

    var rootDir: MediaDir? = null
        private set

    private val allFiles = mutableListOf<MediaFile>()
    /** Map<tagType, Map<tagValue, files>> */
    private val filesById3Tag: MutableMap<ID3TagType, MutableMap<String, MutableList<MediaFile>>> = mutableMapOf()
    private val filesByUsertag: MutableMap<String, MutableList<MediaFile>> = mutableMapOf()
    private val indexLock = Mutex()// only for protecting concurrent writes

    suspend fun scan() {
        if(root.toString() == "") return

        allFiles.clear()
        filesById3Tag.clear()
        filesByUsertag.clear()

        rootDir = scanDir(SystemFileSystem.resolve(root), null)
    }

    override fun fileExists(path: String): Boolean {
        return SystemFileSystem.exists(Path(path))
    }

    override fun getAllFiles(): Sequence<MediaFile> {
        return allFiles.asSequence()
    }

    override fun findFilesWithId3Tag(type: String, value: String): Sequence<MediaFile> {
        val typeValue = ID3TagType.entries.find { it.name == type } ?: let {
            Logger.warn("MediaLibrary::findFilesWithId3Tag", "unknown type: $type")
            return emptySequence()
        }
        return filesById3Tag.get(typeValue)?.get(value)?.asSequence() ?: emptySequence()
    }

    override fun findFilesWithUsertags(tags: List<String>): Map<MediaFile, List<String>> {
        return filesByUsertag.filter {
            tags.contains(it.key)
        }.values.flatten().toHashSet().associateWith {
            it.userTags.intersect(tags).toList()
        }
    }

    private suspend fun scanDir(path: Path, parent: MediaDir?): MediaDir {
        val dir = MediaDirImpl(path, parent)

        val (dirs: List<Path>, files: List<Path>) = SystemFileSystem.list(path).partition {
            SystemFileSystem.metadataOrNull(it)?.isDirectory ?: false
        }

        processFiles(dir, files)
        processDirs(dir, dirs)

        return dir
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun processFiles(parent: MediaDirImpl, files: List<Path>) {
        files.asFlow().chunked(FILES_CHUNK_SIZE).map { chunk ->
            coroutineScope {
                async {
                    chunk.map {
                        parseFile(it, parent)
                    }
                }
            }
        }.collect {
            it.await().forEach {
                parent.addFile(it)
                putFileInIndex(it)
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun parseFile(path: Path, parent: MediaDir): MediaFile {
        val parsedData = try {
            TagParser.parseFile(path.toString())
        } catch(e: Throwable) {
            Logger.error("MediaLibrary::parseFile", "TagParser threw exception", e, false)
            return MediaFileImpl(path, MediaFile.Type.OTHER, parent)
        }

        val file = MediaFileImpl(path, parsedData.type, parent)
        file.mediaTags = parsedData.tags
        file.chapters = parsedData.chapters
        file.userTags = parsedData.usertags.tags

        return file
    }

    private suspend fun putFileInIndex(file: MediaFile) {
        indexLock.withLock {
            allFiles.add(file)

            file.mediaTags.let { tags ->
                tags.title?.let { putFileInId3tagIndex(ID3TagType.TITLE, it, file) }
                tags.artist?.let { putFileInId3tagIndex(ID3TagType.ARTIST, it, file) }
                tags.genre?.let { putFileInId3tagIndex(ID3TagType.GENRE, it, file) }
            }

            file.userTags.forEach { tag ->
                filesByUsertag.getOrPut(tag, { mutableListOf() }).add(file)
            }
        }
    }

    private fun putFileInId3tagIndex(tagType: ID3TagType, tagValue: String, file: MediaFile) {
        filesById3Tag.getOrPut(tagType, { mutableMapOf() })
            .getOrPut(tagValue, { mutableListOf() })
            .add(file)
    }

    private suspend fun processDirs(parent: MediaDirImpl, dirs: List<Path>) {
        dirs.asFlow().map {
            coroutineScope {
                async {
                    scanDir(it, parent)
                }
            }
        }.collect {
            val child = it.await()
            parent.addDir(child)
        }
    }
}
