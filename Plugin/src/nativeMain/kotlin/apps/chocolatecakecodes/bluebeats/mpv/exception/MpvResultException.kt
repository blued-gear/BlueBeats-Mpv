package apps.chocolatecakecodes.bluebeats.mpv.exception

internal class MpvResultException(
    val errCode: Int,
    val msg: String,
) : MpvException("Mpv invocation returned error $errCode; $msg")
