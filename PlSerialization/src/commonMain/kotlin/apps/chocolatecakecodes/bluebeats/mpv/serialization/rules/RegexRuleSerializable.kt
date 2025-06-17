package apps.chocolatecakecodes.bluebeats.mpv.serialization.rules

import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.RegexRule
import apps.chocolatecakecodes.bluebeats.mpv.serialization.FsTools
import kotlinx.serialization.Serializable

@Serializable
@ConsistentCopyVisibility
data class RegexRuleSerializable private constructor(
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

    override fun unpack(fs: FsTools): RegexRule {
        return RegexRule(
            id,
            true,
            attribute,
            regex,
            share.unpack()
        )
    }
}
