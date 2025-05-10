@file:OptIn(ExperimentalNativeApi::class, ExperimentalForeignApi::class)

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import vlccore.VLC_EGENERIC
import vlccore.vlc_object_t
import kotlin.experimental.ExperimentalNativeApi

@CName("k_open")
@Suppress("unused")
fun open(obj: CPointer<vlc_object_t>) : Int {
    println("called open()")
    return VLC_EGENERIC
}

@CName("k_close")
@Suppress("unused")
fun close(obj: CPointer<vlc_object_t>) {
    println("called close()")
}
