package apps.chocolatecakecodes.bluebeats.mpv.serialisation.rules

import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.RegexRule
import apps.chocolatecakecodes.bluebeats.mpv.media.MediaLibraryImpl
import kotlinx.serialization.Serializable

@Serializable
@ConsistentCopyVisibility
internal data class RegexRuleSerializable private constructor(
    val id: Long,
    val attribute: RegexRule.Attribute,
    val regex: String,
    val share: ShareSerializable,
) : RuleSerializable {

    constructor(rule: RegexRule) : this(
        rule.id,
        rule.attribute,
        rule.regex,
        ShareSerializable(rule.share)
    )

    override fun unpack(ml: MediaLibraryImpl): RegexRule {
        return RegexRule(
            id,
            true,
            attribute,
            regex,
            share.unpack()
        )
    }
}
