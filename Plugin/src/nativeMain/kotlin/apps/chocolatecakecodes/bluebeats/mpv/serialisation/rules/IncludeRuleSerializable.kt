package apps.chocolatecakecodes.bluebeats.mpv.serialisation.rules

import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaDir
import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaFile
import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.IncludeRule
import apps.chocolatecakecodes.bluebeats.blueplaylists.utils.castToOrNull
import apps.chocolatecakecodes.bluebeats.mpv.media.MediaLibraryImpl
import apps.chocolatecakecodes.bluebeats.mpv.utils.Logger
import kotlinx.serialization.Serializable

@Serializable
@ConsistentCopyVisibility
internal data class IncludeRuleSerializable private constructor(
    val id: Long,
    val dirs: List<Pair<String, Boolean>>,
    val files: List<String>,
    val share: ShareSerializable,
) : RuleSerializable {

    constructor(rule: IncludeRule, ml: MediaLibraryImpl) : this(
        rule.id,
        rule.getDirs().map { Pair(ml.relativizePath(it.first), it.second) },
        rule.getFiles().map { ml.relativizePath(it) },
        ShareSerializable(rule.share)
    )

    override fun unpack(ml: MediaLibraryImpl): IncludeRule {
        val resolvedDirs = dirs.mapNotNull { (path, deep) ->
            ml.resolvePath(path)?.castToOrNull<MediaDir>()?.let {
                Pair(it, deep)
            } ?: let {
                Logger.warn("IncludeRuleSerializable", "unable to resolve dir $path")
                null
            }
        }.toSet()
        val resolvedFiles = files.mapNotNull { path ->
            ml.resolvePath(path)?.castToOrNull<MediaFile>() ?: let {
                Logger.warn("IncludeRuleSerializable", "unable to resolve file $path")
                null
            }
        }.toSet()

        return IncludeRule(id, true, resolvedDirs, resolvedFiles, share.unpack())
    }
}
