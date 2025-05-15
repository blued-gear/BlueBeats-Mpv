package apps.chocolatecakecodes.bluebeats.vlc.utils

import kotlinx.cinterop.*
import kotlinx.io.Buffer
import kotlinx.io.IOException
import kotlinx.io.RawSource
import platform.posix.size_t
import vlccore.stream_t
import vlccore.vlc_stream_Read
import kotlin.experimental.ExperimentalNativeApi
import kotlin.math.min

@ExperimentalForeignApi
internal class VlcStreamSource(
    private val stream: CPointer<stream_t>,
) : RawSource {

    private val readBuf = ByteArray(2048)

    @OptIn(ExperimentalNativeApi::class)
    override fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        if(byteCount < 1)
            throw IllegalArgumentException("byteCount must be > 0")

        val readCount: Long = vlc_stream_Read(stream.pointed.p_source, readBuf.refTo(0),
            min(byteCount.convert<size_t>(), readBuf.size.convert()))

        if(readCount == 0L) return -1
        if(readCount < 0) throw IOException("vlc_stream_Read returned error $readCount")
        assert(readCount < Int.MAX_VALUE)

        sink.write(readBuf, 0, readCount.toInt())
        return readCount
    }

    override fun close() {
        // NOOP
    }
}
