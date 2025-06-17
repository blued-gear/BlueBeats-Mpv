package apps.chocolatecakecodes.bluebeats.mpv.serialization.rules

import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.GenericRule
import apps.chocolatecakecodes.bluebeats.mpv.serialization.FsTools
import kotlinx.serialization.Serializable

@Serializable
sealed interface RuleSerializable {

    fun unpack(fs: FsTools): GenericRule
}
