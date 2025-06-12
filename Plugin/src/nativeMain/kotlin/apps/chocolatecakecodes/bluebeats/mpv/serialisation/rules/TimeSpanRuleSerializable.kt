package apps.chocolatecakecodes.bluebeats.mpv.serialisation.rules

import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaFile
import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaNode
import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.TimeSpanRule
import apps.chocolatecakecodes.bluebeats.blueplaylists.utils.castToOrNull
import apps.chocolatecakecodes.bluebeats.mpv.media.MediaLibraryImpl
import apps.chocolatecakecodes.bluebeats.mpv.utils.Logger
import kotlinx.serialization.Serializable

@Serializable
@ConsistentCopyVisibility
internal data class TimeSpanRuleSerializable private constructor(
    val id: Long,
    val file: String,
    val startMs: Long,
    val endMs: Long,
    val description: String,
    val share: ShareSerializable,
) : RuleSerializable {

    constructor(rule: TimeSpanRule, ml: MediaLibraryImpl) : this(
        rule.id,
        ml.relativizePath(rule.file),
        rule.startMs,
        rule.endMs,
        rule.description,
        ShareSerializable(rule.share)
    )

    override fun unpack(ml: MediaLibraryImpl): TimeSpanRule {
        val file = ml.resolvePath(this.file)?.castToOrNull<MediaFile>() ?: let {
            Logger.warn("TimeSpanRuleSerializable", "unable to resolve file ${this.file}")
            MediaNode.INVALID_FILE
        }

        return TimeSpanRule(
            id,
            true,
            file,
            startMs,
            endMs,
            description,
            share.unpack()
        )
    }
}
