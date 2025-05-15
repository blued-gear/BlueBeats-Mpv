@file:OptIn(ExperimentalNativeApi::class, ExperimentalForeignApi::class)

import apps.chocolatecakecodes.bluebeats.vlc.serialisation.Test
import apps.chocolatecakecodes.bluebeats.vlc.utils.VlcStreamSource
import kotlinx.cinterop.*
import kotlinx.io.buffered
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.decodeFromSource
import vlccore.*
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalSerializationApi::class)
@CName("k_open")
@Suppress("unused")
fun open(obj: CPointer<vlc_object_t>) : Int {
    println("#### called open()")
    @Suppress("UNCHECKED_CAST")
    val stream = obj as CPointer<stream_t>

    if(!stream_HasExtension(stream, ".bbdp"))
        return VLC_EGENERIC
    if(!checkFileContents(stream))
        return VLC_EGENERIC

    try {
        val a = Json.decodeFromSource<Test>(VlcStreamSource(stream).buffered())
        println("#### deserialized: ")
        println(a)
    } catch(e: Exception) {
        println("#### err in decode")
        e.printStackTrace()
        return VLC_EBADVAR
    }

    return VLC_SUCCESS
}

@CName("k_close")
@Suppress("unused")
fun close(obj: CPointer<vlc_object_t>) {
    println("#### called close()")
}

private fun checkFileContents(stream: CPointer<stream_t>): Boolean {
    val peekBuf = nativeHeap.allocPointerTo<UByteVar>()
    val read = vlc_stream_Peek(stream.pointed.p_source, peekBuf.ptr, 1UL)
    if(read != 1L) {
        nativeHeap.free(peekBuf)
        return false
    }

    val buf = ByteArray(1)
    buf[0] = peekBuf.value!!.get(0).toByte()
    nativeHeap.free(peekBuf)

    val firstContents = buf.decodeToString()
    return firstContents == "{"
}
