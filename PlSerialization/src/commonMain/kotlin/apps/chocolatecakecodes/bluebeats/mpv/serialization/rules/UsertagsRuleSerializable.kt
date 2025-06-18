package apps.chocolatecakecodes.bluebeats.mpv.serialization.rules

import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.UsertagsRule
import apps.chocolatecakecodes.bluebeats.mpv.serialization.FsTools
import kotlinx.serialization.Serializable

@Serializable
@ConsistentCopyVisibility
data class UsertagsRuleSerializable private constructor(
    val id: Long,
    val name: String,
    val combineWithAnd: Boolean,
    val tags: Set<String>,
    val share: ShareSerializable,
) : RuleSerializable {

    constructor(rule: UsertagsRule) : this(
        rule.id,
        rule.name,
        rule.combineWithAnd,
        rule.getTags(),
        ShareSerializable(rule.share)
    )

    override fun unpack(fs: FsTools): UsertagsRule {
        return UsertagsRule(
            share.unpack(),
            combineWithAnd,
            true,
            id,
            name
        ).apply {
            this@UsertagsRuleSerializable.tags.forEach { addTag(it) }
        }
    }
}
