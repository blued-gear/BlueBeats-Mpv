package apps.chocolatecakecodes.bluebeats.mpv.utils

import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.log.Logger

internal object Logger : Logger {

    override fun info(tag: String, message: String) {
        printMsg("INFO: $tag: $message")
    }

    override fun warn(tag: String, message: String) {
        printMsg("WARN: $tag: $message")
    }

    override fun error(tag: String, message: String) {
        printMsg("ERR: $tag: $message")
    }

    override fun error(tag: String, message: String, throwable: Throwable) {
        error(tag, message, throwable, true)
    }

    fun error(tag: String, message: String, throwable: Throwable, printStacktrace: Boolean) {
        printMsg("ERR: $tag: $message")
        if(printStacktrace) {
            throwable.printStackTrace()
            println()
        } else {
            println(throwable)
        }
    }

    fun debug(tag: String, message: String) {
        printMsg("DEBUG: $tag: $message")
    }

    private fun printMsg(str: String) {
        println("[BlueBeats] $str")
    }
}
