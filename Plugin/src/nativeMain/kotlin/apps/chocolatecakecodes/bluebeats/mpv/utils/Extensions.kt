package apps.chocolatecakecodes.bluebeats.mpv.utils

import apps.chocolatecakecodes.bluebeats.mpv.exception.MpvException
import kotlinx.cinterop.ByteVarOf
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString

@OptIn(ExperimentalForeignApi::class)
fun Int.checkReturnCode(context: String, allowGreaterZero: Boolean = false): Int {
    if(this != 0) {
        if(this < 0) {
            val reason = mpv.mpv_error_string(this)?.toKString() ?: "null_msg"
            throw MpvException("EventHandler: $context resulted in return-code $this ($reason)")
        } else if(!allowGreaterZero) {
            throw MpvException("EventHandler: $context resulted in return-code $this")
        }
    }
    return this
}

@ExperimentalForeignApi
fun CPointer<ByteVarOf<Byte>>?.consumeMpvStr(): String? {
    if(this == null) return null
    val str = this.toKString()
    mpv.mpv_free(this)
    return str
}
