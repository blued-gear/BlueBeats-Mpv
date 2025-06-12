package apps.chocolatecakecodes.bluebeats.mpv.serialisation.rules

import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.ID3TagsRule
import apps.chocolatecakecodes.bluebeats.mpv.media.MediaLibraryImpl
import kotlinx.serialization.Serializable

@Serializable
@ConsistentCopyVisibility
internal data class ID3TagsRuleSerializable private constructor(
    val id: Long,
    val tagType: String,
    val tagValues: Set<String>,
    val share: ShareSerializable,
) : RuleSerializable {

    constructor(rule: ID3TagsRule) : this(
        rule.id,
        rule.tagType,
        rule.getTagValues(),
        ShareSerializable(rule.share)
    )

    override fun unpack(ml: MediaLibraryImpl): ID3TagsRule {
        return ID3TagsRule(
            share.unpack(),
            true,
            id
        ).apply {
            this@apply.tagType = this@ID3TagsRuleSerializable.tagType
            this@ID3TagsRuleSerializable.tagValues.forEach { addTagValue(it) }
        }
    }
}
