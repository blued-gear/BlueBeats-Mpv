package apps.chocolatecakecodes.bluebeats.mpv.serialization.rules

import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.Share
import kotlinx.serialization.Serializable

@Serializable
@ConsistentCopyVisibility
data class ShareSerializable private constructor(
    val value: Float,
    val isRelative: Boolean
) {

    constructor(share: Share) : this(share.value, share.isRelative)

    fun unpack(): Share = Share(value, isRelative)
}
