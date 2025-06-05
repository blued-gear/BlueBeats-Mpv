package apps.chocolatecakecodes.bluebeats.mpv.serialisation.rules

import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.Rule
import kotlinx.serialization.Serializable

@Serializable
@ConsistentCopyVisibility
internal data class ShareSerializable private constructor(
    val value: Float,
    val isRelative: Boolean
) {

    constructor(share: Rule.Share) : this(share.value, share.isRelative)

    fun unpack(): Rule.Share = Rule.Share(value, isRelative)
}
