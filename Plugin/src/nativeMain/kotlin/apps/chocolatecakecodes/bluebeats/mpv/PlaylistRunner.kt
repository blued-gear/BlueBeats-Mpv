package apps.chocolatecakecodes.bluebeats.mpv

import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaLibrary
import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.PlaylistIterator
import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.DynamicPlaylist
import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.DynamicPlaylistIterator
import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.items.PlaylistItem
import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.items.TimeSpanItem
import apps.chocolatecakecodes.bluebeats.blueplaylists.utils.castToOrNull
import apps.chocolatecakecodes.bluebeats.mpv.media.MediaLibraryImpl
import apps.chocolatecakecodes.bluebeats.mpv.mpvevents.AsyncCommand
import apps.chocolatecakecodes.bluebeats.mpv.mpvevents.EventConsumer
import apps.chocolatecakecodes.bluebeats.mpv.player.PlayerControl
import apps.chocolatecakecodes.bluebeats.mpv.serialization.DynPl
import apps.chocolatecakecodes.bluebeats.mpv.serialization.FsTools
import apps.chocolatecakecodes.bluebeats.mpv.serialization.Serializer
import apps.chocolatecakecodes.bluebeats.mpv.utils.Logger
import apps.chocolatecakecodes.bluebeats.mpv.utils.MpvPropertyHelper
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.io.decodeFromSource
import mpv.mpv_event

@OptIn(ExperimentalForeignApi::class)
internal class PlaylistRunner(
    private val pluginContext: PluginContext,
    private val plFile: String,
) : EventConsumer {

    override val requestedEvents: Set<UInt> = setOf(mpv.MPV_EVENT_START_FILE)

    private val player = PlayerControl(pluginContext)
    private lateinit var pl: DynamicPlaylist
    private lateinit var plIter: DynamicPlaylistIterator
    private val plItems: MutableList<PlaylistItem> = mutableListOf()
    private var lastPlayedItem: PlaylistItem? = null
    private var mpvPlInited = false

    suspend fun start() {
        pluginContext.eventHandler.registerEventConsumer(this)

        initPlaylist()
        startPlayback()
    }

    override suspend fun onEvent(eventId: UInt, event: CPointer<mpv_event>) {
        when(eventId) {
            mpv.MPV_EVENT_START_FILE -> onFileStarted()
        }
    }

    override suspend fun terminate() {}

    private suspend fun stop() {
        pluginContext.eventHandler.deregisterEventConsumer(this)
    }

    private suspend fun initPlaylist() {
        val pl = loadFile()
        if(pl == null) {
            Logger.error("PlaylistRunner", "failed to load bbdp file")
            return
        }
        Logger.info("PlaylistRunner", "loaded bbdp file")

        val ml = loadMediaLib(pl.mediaRoot)
        loadPlaylist(pl, ml).let { (pl, iter) ->
            this.pl = pl
            this.plIter = iter
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

    private suspend fun loadMediaLib(rootPath: String): MediaLibraryImpl {
        Logger.info("PlaylistRunner", "loading MediaLibrary ...")
        val ml = MediaLibraryImpl(Path(rootPath))
        ml.scan()
        MediaLibrary.Slot.INSTANCE = ml
        Logger.info("PlaylistRunner", "loaded MediaLibrary")
        return ml
    }

    private fun loadPlaylist(serializedPl: DynPl, ml: MediaLibraryImpl): Pair<DynamicPlaylist, DynamicPlaylistIterator> {
        val rootRule = serializedPl.rootRule.unpack(FsTools(ml.rootDir!!))
        val pl = DynamicPlaylist(1, "Playlist", rootRule).apply {
            iterationSize = serializedPl.iterationSize
        }
        val iter = pl.getIterator(PlaylistIterator.RepeatMode.ALL, true) as DynamicPlaylistIterator
        return Pair(pl, iter)
    }

    private suspend fun startPlayback() {
        // need to start a file immediately or else player will exit
        val firstItem = plIter.currentItem()
        if(firstItem is PlaylistItem.INVALID) {
            Logger.error("PlaylistRunner", "DynamicPlaylist is empty; stopping runner")
            stop()
            return
        }
        player.useStreamOpenProp = true
        player.playMedia(firstItem.file!!, true)
        player.useStreamOpenProp = false

        fillPlaylist()
    }

    private suspend fun onFileStarted() {
        lastPlayedItem?.castToOrNull<TimeSpanItem>()?.runningController?.unregister()

        if(!isOwnPlaylist()) {
            Logger.info("PlaylistRunner", "playlist changed; stopping runner")
            stop()
            return
        }

        val plIdx = MpvPropertyHelper.getLong(pluginContext.mpvCtx, "playlist-pos").toInt()
        lastPlayedItem = plItems[plIdx].also {
            // Mpv should ignore setting the same path, so this can be used to activate e.g. TimeSpanItemPlayerController
            it.play(player)
        }

        // refill playlist when playing last item
        if(plIdx == plItems.lastIndex)
            fillPlaylist()
    }

    private suspend fun fillPlaylist() {
        CoroutineScope(currentCoroutineContext().job).launch {
            Logger.info("PlaylistRunner", "refilling playlist")
            AsyncCommand.issueCommandRetUnit("playlist-clear").await()

            // save current item as it will be prepended in the mpv-playlist
            val currentItem = let {
                val plIdx = MpvPropertyHelper.getLong(pluginContext.mpvCtx, "playlist-pos").toInt()
                return@let if(plIdx >= 0 && plItems.isNotEmpty())
                    plItems[plIdx]
                else
                    null
            }
            plItems.clear()
            currentItem?.let { plItems.add(it) }

            repeat(pl.iterationSize) {
                val item = plIter.nextItem()
                if(item is PlaylistItem.INVALID) {
                    Logger.warn("PlaylistRunner", "got invalid PlaylistItem")
                    return@repeat
                }
                plItems.add(item)

                val loadMode: String
                if(mpvPlInited) {
                    loadMode = "append-play"
                } else {
                    loadMode = "replace"
                    mpvPlInited = true
                }

                AsyncCommand.issueCommandRetUnit("loadfile", item.file!!.path, loadMode).await()
            }
        }
    }

    private fun isOwnPlaylist(): Boolean {
        val plIdx = MpvPropertyHelper.getLong(pluginContext.mpvCtx, "playlist-pos").toInt()
        if(plIdx == -1 || plIdx > plItems.lastIndex) return false
        val path = MpvPropertyHelper.getString(pluginContext.mpvCtx, "path")
        return plItems[plIdx].file?.path == path
    }
}
