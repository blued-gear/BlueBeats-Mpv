package apps.chocolatecakecodes.bluebeats.mpv.mpvevents

import apps.chocolatecakecodes.bluebeats.blueplaylists.utils.castTo
import apps.chocolatecakecodes.bluebeats.mpv.PluginContext
import apps.chocolatecakecodes.bluebeats.mpv.exception.MpvResultException
import apps.chocolatecakecodes.bluebeats.mpv.utils.Logger
import apps.chocolatecakecodes.bluebeats.mpv.utils.checkReturnCode
import kotlinx.cinterop.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.cancel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndIncrement

/**
 * Helper to call async commands and wait for their replies with a single call.
 * The returned Deferred must not be awaited if this would block returns to EventHandler.
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalAtomicApi::class)
internal object AsyncCommand : EventConsumer {

    override val requestedEvents: Set<UInt> = setOf(mpv.MPV_EVENT_COMMAND_REPLY)

    private lateinit var pluginCtx: PluginContext
    private val callId = AtomicLong(1)
    private val callDeferreds: MutableMap<ULong, CompletableDeferred<*>> = mutableMapOf()
    private val callDeferredsMutex = Mutex()

    fun init(pluginCtx: PluginContext): AsyncCommand {
        this.pluginCtx = pluginCtx
        return this
    }

    /**
     * issues an async command which will return Unit
     */
    suspend fun issueCommandRetUnit(vararg args: String): Deferred<Unit> {
        val id = callId.fetchAndIncrement().toULong()
        val deferred = CompletableDeferred<Unit>()
        callDeferredsMutex.withLock { callDeferreds.put(id, deferred) }

        memScoped {
            val argsPtr = args.map { it.cstr.getPointer(this) }.plus(null).toCValues()
            mpv.mpv_command_async(pluginCtx.mpvCtx, id, argsPtr)
                .checkReturnCode("[AsyncCommand] mpv_command_async (args: ${args.contentToString()})")
        }

        return deferred
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun onEvent(eventId: UInt, event: CPointer<mpv.mpv_event>) {
        val deferred = callDeferredsMutex.withLock {
            callDeferreds.remove(event.pointed.reply_userdata)
        } ?: return

        event.pointed.error.let { err ->
            if(err != 0) {
                deferred.completeExceptionally(MpvResultException(err, "command returned with non-0 error"))
                return
            }
        }

        val commandEvent = event.pointed.data!!.castTo<CPointer<mpv.mpv_event_command>>().pointed
        val result = commandEvent.result
        when(result.format) {
            mpv.MPV_FORMAT_NONE -> {
                deferred.castTo<CompletableDeferred<Unit>>().complete(Unit)
            }
            else -> {
                Logger.warn("AsyncCommand", "command result contains unsupported format ${result.format}; falling back to Unit")
                deferred.castTo<CompletableDeferred<Unit>>().complete(Unit)
            }
        }
    }

    override suspend fun terminate() {
        callDeferredsMutex.withLock {
            callDeferreds.values.forEach { it.cancel("terminated") }
            callDeferreds.clear()
        }
    }
}
