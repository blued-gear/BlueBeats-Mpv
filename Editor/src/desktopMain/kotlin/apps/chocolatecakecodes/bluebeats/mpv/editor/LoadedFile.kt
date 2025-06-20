package apps.chocolatecakecodes.bluebeats.mpv.editor

import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaNode
import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.GenericRule
import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.RuleGroup
import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.Share
import apps.chocolatecakecodes.bluebeats.mpv.editor.media.FsTools
import apps.chocolatecakecodes.bluebeats.mpv.editor.media.MediaLibrary
import apps.chocolatecakecodes.bluebeats.mpv.serialization.DynPl
import apps.chocolatecakecodes.bluebeats.mpv.serialization.Serializer
import apps.chocolatecakecodes.bluebeats.mpv.serialization.rules.RuleGroupSerializable
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.math.max

internal object LoadedFile {

    lateinit var filePath: String
        private set
    lateinit var pl: DynPl
        private set
    lateinit var rootGroup: RuleGroup
        private set
    lateinit var mediaLib: MediaLibrary
        private set

    private var nextFreeId: Long = 0

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
        nextFreeId = 2
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun load(path: String) {
        FileInputStream(path).use { inp ->
            pl = Serializer.json.decodeFromStream<DynPl>(inp)
            mediaLib = MediaLibrary(pl.mediaRoot)
            rootGroup = pl.rootRule.unpack(FsTools(mediaLib.rootDir))
            filePath = path
        }

        fun maxId(max: Long, rule: GenericRule): Long {
            var newMax = max(max, rule.id)
            if(rule is RuleGroup) {
                newMax = rule.getRules().fold(newMax) { curMax, (rule, _) ->
                    maxId(curMax, rule)
                }
            }
            return newMax
        }
        nextFreeId = maxId(0, rootGroup) + 1
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun save(path: String? = null) {
        pl = pl.copy(rootRule = RuleGroupSerializable(rootGroup, FsTools(mediaLib.rootDir)))
        FileOutputStream(path ?: filePath).use {
            Serializer.json.encodeToStream(pl, it)
        }
    }

    fun setMediaRootPath(mediaRootPath: String) {
        mediaLib = MediaLibrary(mediaRootPath)
        updatePl { it.copy(mediaRoot = mediaRootPath) }
    }

    fun updatePl(block: (DynPl) -> DynPl) {
        pl = block(pl)
    }

    fun getFreeId(): Long {
        val id = nextFreeId
        nextFreeId++
        return id
    }
}
