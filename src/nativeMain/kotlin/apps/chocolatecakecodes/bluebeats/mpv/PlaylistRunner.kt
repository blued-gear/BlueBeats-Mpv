package apps.chocolatecakecodes.bluebeats.mpv

import apps.chocolatecakecodes.bluebeats.mpv.serialisation.DynPl
import apps.chocolatecakecodes.bluebeats.mpv.serialisation.Serializer
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
                    println("### failed to load bbdp file")
                    return@launch
                }
                println("### loaded file: $pl")
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun loadFile(): DynPl? {
        SystemFileSystem.source(Path(plFile)).buffered().use { source ->
            try {
                return Serializer.json.decodeFromSource<DynPl>(source)
            } catch(e: Exception) {
                println("### invalid bbdp file")
                e.printStackTrace()

                return null
            }
        }
    }

    private fun loadMediaDb() {

    }

    private fun startPlaylist() {

    }
}
