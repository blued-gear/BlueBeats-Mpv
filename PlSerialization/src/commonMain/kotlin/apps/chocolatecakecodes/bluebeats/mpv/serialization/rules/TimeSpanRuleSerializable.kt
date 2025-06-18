package apps.chocolatecakecodes.bluebeats.mpv.serialization.rules

import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.log.Logger
import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaFile
import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaNode
import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.TimeSpanRule
import apps.chocolatecakecodes.bluebeats.blueplaylists.utils.castToOrNull
import apps.chocolatecakecodes.bluebeats.mpv.serialization.FsTools
import kotlinx.serialization.Serializable

@Serializable
@ConsistentCopyVisibility
data class TimeSpanRuleSerializable private constructor(
    val id: Long,
    val name: String,
    val file: String,
    val startMs: Long,
    val endMs: Long,
    val description: String,
    val share: ShareSerializable,
) : RuleSerializable {

    constructor(rule: TimeSpanRule, fs: FsTools) : this(
        rule.id,
        rule.name,
        fs.relativizePath(rule.file),
        rule.startMs,
        rule.endMs,
        rule.description,
        ShareSerializable(rule.share)
    )

    override fun unpack(fs: FsTools): TimeSpanRule {
        val file = fs.resolvePath(this.file)?.castToOrNull<MediaFile>() ?: let {
            Logger.Slot.INSTANCE.warn("TimeSpanRuleSerializable", "unable to resolve file ${this.file}")
            MediaNode.INVALID_FILE
        }

        return TimeSpanRule(
            id,
            true,
            file,
            startMs,
            endMs,
            description,
            share.unpack(),
            name
        )
    }
}
