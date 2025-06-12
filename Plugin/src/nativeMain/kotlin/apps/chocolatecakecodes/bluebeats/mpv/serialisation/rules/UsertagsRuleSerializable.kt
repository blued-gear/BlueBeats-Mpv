package apps.chocolatecakecodes.bluebeats.mpv.serialisation.rules

import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.UsertagsRule
import apps.chocolatecakecodes.bluebeats.mpv.media.MediaLibraryImpl
import kotlinx.serialization.Serializable

@Serializable
@ConsistentCopyVisibility
internal data class UsertagsRuleSerializable private constructor(
    val id: Long,
    val combineWithAnd: Boolean,
    val tags: Set<String>,
    val share: ShareSerializable,
) : RuleSerializable {

    constructor(rule: UsertagsRule) : this(
        rule.id,
        rule.combineWithAnd,
        rule.getTags(),
        ShareSerializable(rule.share)
    )

    override fun unpack(ml: MediaLibraryImpl): UsertagsRule {
        return UsertagsRule(
            share.unpack(),
            combineWithAnd,
            true,
            id
        ).apply {
            this@UsertagsRuleSerializable.tags.forEach { addTag(it) }
        }
    }
}
