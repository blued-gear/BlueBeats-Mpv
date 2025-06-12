package apps.chocolatecakecodes.bluebeats.mpv.serialisation.rules

import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.*
import apps.chocolatecakecodes.bluebeats.mpv.media.MediaLibraryImpl
import kotlinx.serialization.Serializable

@Serializable
@ConsistentCopyVisibility
internal data class RuleGroupSerializable private constructor(
    val id: Long,
    val share: ShareSerializable,
    val combineWithAnd: Boolean,
    val rules: List<RuleGroupItem>
) : RuleSerializable {

    companion object {

        private fun packRule(rule: GenericRule, ml: MediaLibraryImpl): RuleSerializable {
            return when(rule) {
                is RuleGroup -> RuleGroupSerializable(rule, ml)
                is IncludeRule -> IncludeRuleSerializable(rule, ml)
                is ID3TagsRule -> ID3TagsRuleSerializable(rule)
                is RegexRule -> RegexRuleSerializable(rule)
                is TimeSpanRule -> TimeSpanRuleSerializable(rule, ml)
                is UsertagsRule -> UsertagsRuleSerializable(rule)
            }
        }
    }

    constructor(rule: RuleGroup, ml: MediaLibraryImpl) : this(
        rule.id,
        ShareSerializable(rule.share),
        rule.combineWithAnd,
        rule.getRules().map { RuleGroupItem(rule = packRule(it.first, ml), negate = it.second) }
    )

    override fun unpack(ml: MediaLibraryImpl) = RuleGroup(
        id,
        true,
        share.unpack(),
        combineWithAnd,
        rules.map { Pair(it.rule.unpack(ml), it.negate) }
    )
}

@Serializable
internal data class RuleGroupItem(
    val negate: Boolean,
    val rule: RuleSerializable,
)
