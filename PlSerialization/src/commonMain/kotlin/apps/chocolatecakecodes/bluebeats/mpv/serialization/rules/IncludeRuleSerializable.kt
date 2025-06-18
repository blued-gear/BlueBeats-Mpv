package apps.chocolatecakecodes.bluebeats.mpv.serialization.rules

import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.log.Logger
import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaDir
import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaFile
import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.IncludeRule
import apps.chocolatecakecodes.bluebeats.blueplaylists.utils.castToOrNull
import apps.chocolatecakecodes.bluebeats.mpv.serialization.FsTools
import kotlinx.serialization.Serializable

@Serializable
@ConsistentCopyVisibility
data class IncludeRuleSerializable private constructor(
    val id: Long,
    val name: String,
    val dirs: List<Pair<String, Boolean>>,
    val files: List<String>,
    val share: ShareSerializable,
) : RuleSerializable {

    constructor(rule: IncludeRule, fs: FsTools) : this(
        rule.id,
        rule.name,
        rule.getDirs().map { Pair(fs.relativizePath(it.first), it.second) },
        rule.getFiles().map { fs.relativizePath(it) },
        ShareSerializable(rule.share)
    )

    override fun unpack(fs: FsTools): IncludeRule {
        val resolvedDirs = dirs.mapNotNull { (path, deep) ->
            fs.resolvePath(path)?.castToOrNull<MediaDir>()?.let {
                Pair(it, deep)
            } ?: let {
                Logger.Slot.INSTANCE.warn("IncludeRuleSerializable", "unable to resolve dir $path")
                null
            }
        }.toSet()
        val resolvedFiles = files.mapNotNull { path ->
            fs.resolvePath(path)?.castToOrNull<MediaFile>() ?: let {
                Logger.Slot.INSTANCE.warn("IncludeRuleSerializable", "unable to resolve file $path")
                null
            }
        }.toSet()

        return IncludeRule(id, true, share.unpack(), resolvedDirs, resolvedFiles, name)
    }
}
