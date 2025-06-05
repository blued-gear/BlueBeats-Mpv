package apps.chocolatecakecodes.bluebeats.mpv.serialisation.rules

import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.IncludeRule
import kotlinx.serialization.Serializable

/*
override val id: Long,
    override val isOriginal: Boolean,
    dirs: Set<DirPathInclude> = emptySet(),
    files: Set<MediaFile> = emptySet(),
    initialShare: Rule.Share
 */
@Serializable
@ConsistentCopyVisibility
internal data class IncludeRuleSerializable private constructor(
    val id: Long,
    val dirs: List<Pair<String, Boolean>>,
    val files: List<String>,
    val share: ShareSerializable,
) : RuleSerializable {

    //TODO relativize paths
    constructor(rule: IncludeRule) : this(
        rule.id,
        rule.getDirs().map { Pair(it.first.path, it.second) },
        rule.getFiles().map { it.path },
        ShareSerializable(rule.share)
    )

    override fun unpack() = IncludeRule(
        id,
        true,
        emptySet(),
        emptySet(),
        share.unpack()
    )
}
