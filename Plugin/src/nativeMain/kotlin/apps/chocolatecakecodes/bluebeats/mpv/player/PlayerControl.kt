package apps.chocolatecakecodes.bluebeats.mpv.player

import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.BasicPlayer
import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.MediaFile
import apps.chocolatecakecodes.bluebeats.mpv.PluginContext
import apps.chocolatecakecodes.bluebeats.mpv.mpvevents.AsyncCommand
import apps.chocolatecakecodes.bluebeats.mpv.utils.MpvPropertyHelper
import apps.chocolatecakecodes.bluebeats.mpv.utils.checkReturnCode
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@ExperimentalForeignApi
internal class PlayerControl(
    val pluginContext: PluginContext
) : BasicPlayer {

    var useStreamOpenProp = false

    override fun playMedia(media: MediaFile, keepPlaylist: Boolean) {
        if(useStreamOpenProp) {
            mpv.mpv_set_property_string(pluginContext.mpvCtx, "stream-open-filename", media.path)
                .checkReturnCode("[PlayerControl] setting stream-open-filename")
        } else {
            if(keepPlaylist) {
                val path = MpvPropertyHelper.getString(pluginContext.mpvCtx, "path")
                if(path == media.path) return
            }

            CoroutineScope(Dispatchers.Default).launch {
                AsyncCommand.issueCommandRetUnit("loadfile", media.path, "replace").await()
            }
        }
    }

    suspend fun playNextMedia() {
        AsyncCommand.issueCommandRetUnit("playlist-next", "weak")
    }

    /**
     * @param time time in milliseconds
     */
    fun setTimePos(time: Long) {
        MpvPropertyHelper.setLong(pluginContext.mpvCtx, "time-pos/full", time)
    }

    /**
     * @return time in milliseconds
     */
    fun getTimePos(): Long {
        return MpvPropertyHelper.getLong(pluginContext.mpvCtx, "time-pos/full")
    }
}
