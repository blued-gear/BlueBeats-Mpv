package apps.chocolatecakecodes.bluebeats.mpv.serialization.rules

import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.Rule
import kotlinx.serialization.Serializable

@Serializable
@ConsistentCopyVisibility
data class ShareSerializable private constructor(
    val value: Float,
    val isRelative: Boolean
) {

    constructor(share: Rule.Share) : this(share.value, share.isRelative)

    fun unpack(): Rule.Share = Rule.Share(value, isRelative)
}
