@file:OptIn(ExperimentalNativeApi::class, ExperimentalForeignApi::class)

import cnames.structs.mpv_handle
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalSerializationApi::class)
@CName("mpv_open_cplugin")
@Suppress("unused")
fun main(mpvCtx: CPointer<mpv_handle>): Int {
    println("### plugin loading")

    return 0
}
