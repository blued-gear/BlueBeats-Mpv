package apps.chocolatecakecodes.bluebeats.mpv.serialization.rules

import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.*
import apps.chocolatecakecodes.bluebeats.mpv.serialization.FsTools
import kotlinx.serialization.Serializable

@Serializable
@ConsistentCopyVisibility
data class RuleGroupSerializable private constructor(
    val id: Long,
    val name: String,
    val share: ShareSerializable,
    val combineWithAnd: Boolean,
    val rules: List<RuleGroupItem>
) : RuleSerializable {

    companion object {

        private fun packRule(rule: GenericRule, fs: FsTools): RuleSerializable {
            return when(rule) {
                is RuleGroup -> RuleGroupSerializable(rule, fs)
                is IncludeRule -> IncludeRuleSerializable(rule, fs)
                is ID3TagsRule -> ID3TagsRuleSerializable(rule)
                is RegexRule -> RegexRuleSerializable(rule)
                is TimeSpanRule -> TimeSpanRuleSerializable(rule, fs)
                is UsertagsRule -> UsertagsRuleSerializable(rule)
            }
        }
    }

    constructor(rule: RuleGroup, fs: FsTools) : this(
        rule.id,
        rule.name,
        ShareSerializable(rule.share),
        rule.combineWithAnd,
        rule.getRules().map { RuleGroupItem(rule = packRule(it.first, fs), negate = it.second) }
    )

    override fun unpack(fs: FsTools) = RuleGroup(
        id,
        true,
        share.unpack(),
        name,
        combineWithAnd,
        rules.map { Pair(it.rule.unpack(fs), it.negate) }
    )
}

@Serializable
data class RuleGroupItem(
    val negate: Boolean,
    val rule: RuleSerializable,
)
