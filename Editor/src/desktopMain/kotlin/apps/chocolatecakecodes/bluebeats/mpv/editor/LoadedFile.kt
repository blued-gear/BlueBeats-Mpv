package apps.chocolatecakecodes.bluebeats.mpv.editor

import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.RuleGroup
import apps.chocolatecakecodes.bluebeats.mpv.serialization.DynPl

internal object LoadedFile {

    lateinit var filePath: String
    lateinit var pl: DynPl
    lateinit var rootGroup: RuleGroup

}
