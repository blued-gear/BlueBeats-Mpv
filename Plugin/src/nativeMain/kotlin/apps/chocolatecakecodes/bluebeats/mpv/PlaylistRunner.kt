package apps.chocolatecakecodes.bluebeats.mpv

import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaLibrary
import apps.chocolatecakecodes.bluebeats.mpv.media.MediaLibraryImpl
import apps.chocolatecakecodes.bluebeats.mpv.serialisation.DynPl
import apps.chocolatecakecodes.bluebeats.mpv.serialisation.Serializer
import apps.chocolatecakecodes.bluebeats.mpv.utils.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.io.decodeFromSource

internal class PlaylistRunner(
    private val pluginContext: PluginContext,
    private val plFile: String,
) {

    private var job: Job? = null

    suspend fun run() {
        job = coroutineScope {
            launch {
                val pl = loadFile()
                if(pl == null) {
                    Logger.error("PlaylistRunner", "failed to load bbdp file")
                    return@launch
                }

                Logger.info("PlaylistRunner", "loaded file: $pl")

                loadMediaDb(pl.mediaRoot)
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun loadFile(): DynPl? {
        SystemFileSystem.source(Path(plFile)).buffered().use { source ->
            try {
                return Serializer.json.decodeFromSource<DynPl>(source)
            } catch(e: Exception) {
                Logger.error("PlaylistRunner", "invalid bbdp file", e)
                return null
            }
        }
    }

    private suspend fun loadMediaDb(rootPath: String) {
        Logger.info("PlaylistRunner", "loading MediaLibrary")
        val ml = MediaLibraryImpl(Path(rootPath))
        MediaLibrary.Slot.INSTANCE = ml
        ml.scan()

        Logger.info("PlaylistRunner", "loaded MediaLibrary")
        ml.getAllFiles().forEach {
            println("   $it: ${it.type} , ${it.mediaTags} , ${it.userTags} , ${it.chapters}")
        }
    }

    private fun startPlaylist() {

    }
}
