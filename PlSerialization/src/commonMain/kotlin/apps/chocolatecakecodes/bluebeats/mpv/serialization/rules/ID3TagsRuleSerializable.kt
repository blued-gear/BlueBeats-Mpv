package apps.chocolatecakecodes.bluebeats.mpv.serialization.rules

import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.ID3TagsRule
import apps.chocolatecakecodes.bluebeats.mpv.serialization.FsTools
import apps.chocolatecakecodes.bluebeats.mpv.serialization.misc.ID3TagType
import kotlinx.serialization.Serializable

@Serializable
@ConsistentCopyVisibility
data class ID3TagsRuleSerializable private constructor(
    val id: Long,
    val name: String,
    val tagType: ID3TagType,
    val tagValues: Set<String>,
    val share: ShareSerializable,
) : RuleSerializable {

    constructor(rule: ID3TagsRule) : this(
        rule.id,
        rule.name,
        ID3TagType.entries.find { it.name == rule.tagType } ?: ID3TagType.INVALID,
        rule.getTagValues(),
        ShareSerializable(rule.share)
    )

    override fun unpack(fs: FsTools): ID3TagsRule {
        return ID3TagsRule(
            share.unpack(),
            true,
            id,
            name
        ).apply {
            this@apply.tagType = this@ID3TagsRuleSerializable.tagType.name
            this@ID3TagsRuleSerializable.tagValues.forEach { addTagValue(it) }
        }
    }
}
