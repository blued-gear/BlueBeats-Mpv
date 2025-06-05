package apps.chocolatecakecodes.bluebeats.mpv.serialisation.rules

import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.GenericRule
import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.IncludeRule
import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.RuleGroup
import kotlinx.serialization.Serializable

@Serializable
@ConsistentCopyVisibility
internal data class RuleGroupSerializable private constructor(
    val id: Long,
    val share: ShareSerializable,
    val combineWithAnd: Boolean,
    val rules: List<Pair<RuleSerializable, Boolean>>
) : RuleSerializable {

    companion object {

        private fun packRule(rule: GenericRule): RuleSerializable {
            return when(rule) {
                is RuleGroup -> RuleGroupSerializable(rule)
                is IncludeRule -> IncludeRuleSerializable(rule)
                else -> throw AssertionError("incomplete testing for rule classes")
            }
        }
    }

    constructor(rule: RuleGroup) : this(
        rule.id,
        ShareSerializable(rule.share),
        rule.combineWithAnd,
        rule.getRules().map { Pair(packRule(it.first), it.second) }
    )

    override fun unpack() = RuleGroup(
        id,
        true,
        share.unpack(),
        combineWithAnd,
        rules.map { Pair(it.first.unpack(), it.second) }
    )
}
