package apps.chocolatecakecodes.bluebeats.mpv.utils

import cnames.structs.mpv_handle
import kotlinx.cinterop.*

@ExperimentalForeignApi
object MpvPropertyHelper {

    fun getString(mpvCtx: CPointer<mpv_handle>, name: String): String? {
        return mpv.mpv_get_property_string(mpvCtx, name).consumeMpvStr()
    }

    fun setString(mpvCtx: CPointer<mpv_handle>, name: String, value: String?) {
        mpv.mpv_set_property_string(mpvCtx, name, value)
            .checkReturnCode("MpvPropertyHelper::setString ($name)")
    }

    fun getLong(mpvCtx: CPointer<mpv_handle>, name: String): Long {
        val value: Long
        val valuePtr = nativeHeap.alloc<LongVar>()
        try {
            mpv.mpv_get_property(
                mpvCtx,
                name,
                mpv.MPV_FORMAT_INT64,
                valuePtr.ptr
            ).checkReturnCode("MpvPropertyHelper::getLong ($name)")
            value = valuePtr.value
        } finally {
            nativeHeap.free(valuePtr.rawPtr)
        }
        return value
    }

    fun setLong(mpvCtx: CPointer<mpv_handle>, name: String, value: Long) {
        val valuePtr = nativeHeap.alloc(value)
        try {
            mpv.mpv_set_property(
                mpvCtx,
                name,
                mpv.MPV_FORMAT_INT64,
                valuePtr.ptr
            ).checkReturnCode("MpvPropertyHelper::setLong ($name)")
        } finally {
            nativeHeap.free(valuePtr.rawPtr)
        }
    }

    fun getDouble(mpvCtx: CPointer<mpv_handle>, name: String): Double {
        val value: Double
        val valuePtr = nativeHeap.alloc<DoubleVar>()
        try {
            mpv.mpv_get_property(
                mpvCtx,
                name,
                mpv.MPV_FORMAT_DOUBLE,
                valuePtr.ptr
            ).checkReturnCode("MpvPropertyHelper::getDouble ($name)")
            value = valuePtr.value
        } finally {
            nativeHeap.free(valuePtr.rawPtr)
        }
        return value
    }

    fun setDouble(mpvCtx: CPointer<mpv_handle>, name: String, value: Double) {
        val valuePtr = nativeHeap.alloc(value)
        try {
            mpv.mpv_set_property(
                mpvCtx,
                name,
                mpv.MPV_FORMAT_DOUBLE,
                valuePtr.ptr
            ).checkReturnCode("MpvPropertyHelper::setDouble ($name)")
        } finally {
            nativeHeap.free(valuePtr.rawPtr)
        }
    }

}
