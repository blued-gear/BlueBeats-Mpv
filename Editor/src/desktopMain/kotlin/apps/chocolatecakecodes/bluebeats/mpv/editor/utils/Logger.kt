package apps.chocolatecakecodes.bluebeats.mpv.editor.utils

import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.log.Logger
import java.util.logging.Level

internal object Logger : Logger {

    private val platformLogger = java.util.logging.Logger.getLogger("Logger")

    override fun info(tag: String, message: String) {
        platformLogger.info("$tag: $message")
    }

    override fun warn(tag: String, message: String) {
        platformLogger.warning("$tag: $message")
    }

    override fun error(tag: String, message: String) {
        platformLogger.severe("$tag: $message")
    }

    override fun error(tag: String, message: String, throwable: Throwable) {
        error(tag, message, throwable, true)
    }

    fun error(tag: String, message: String, throwable: Throwable, printStacktrace: Boolean) {
        if(printStacktrace) {
            platformLogger.log(Level.SEVERE, "$tag: $message", throwable)
        } else {
            platformLogger.severe("$tag: $message")
            System.err.println(throwable)
        }
    }

    fun debug(tag: String, message: String) {
        platformLogger.finer("DEBUG: $tag: $message")
    }
}
