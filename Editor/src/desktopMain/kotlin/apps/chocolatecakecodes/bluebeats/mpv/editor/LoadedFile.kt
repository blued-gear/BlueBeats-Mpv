package apps.chocolatecakecodes.bluebeats.mpv.editor

import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaNode
import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.RuleGroup
import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.Share
import apps.chocolatecakecodes.bluebeats.mpv.editor.media.FsTools
import apps.chocolatecakecodes.bluebeats.mpv.editor.media.MediaLibrary
import apps.chocolatecakecodes.bluebeats.mpv.serialization.DynPl
import apps.chocolatecakecodes.bluebeats.mpv.serialization.Serializer
import apps.chocolatecakecodes.bluebeats.mpv.serialization.rules.RuleGroupSerializable
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import java.io.FileInputStream

internal object LoadedFile {

    lateinit var filePath: String
        private set
    lateinit var pl: DynPl
        private set
    lateinit var rootGroup: RuleGroup
        private set
    lateinit var mediaLib: MediaLibrary
        private set

    fun initEmpty(path: String) {
        filePath = path
        rootGroup = RuleGroup(
            1,
            true,
            Share(-1f, true),
            "root",
            false
        )
        pl = DynPl(
            "",
            50,
            RuleGroupSerializable(rootGroup, FsTools(MediaNode.UNSPECIFIED_DIR))
        )
        mediaLib = MediaLibrary("")
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun load(path: String) {
        FileInputStream(path).use { inp ->
            pl = Serializer.json.decodeFromStream<DynPl>(inp)
            mediaLib = MediaLibrary(pl.mediaRoot)
            rootGroup = pl.rootRule.unpack(FsTools(mediaLib.rootDir))
            filePath = path
        }
    }

    fun setMediaRootPath(mediaRootPath: String) {
        mediaLib = MediaLibrary(mediaRootPath)
        updatePl { it.copy(mediaRoot = mediaRootPath) }
    }

    fun updatePl(block: (DynPl) -> DynPl) {
        pl = block(pl)
    }
}
