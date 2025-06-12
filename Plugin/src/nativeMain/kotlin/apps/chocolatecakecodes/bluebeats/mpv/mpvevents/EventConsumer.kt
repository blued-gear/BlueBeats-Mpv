package apps.chocolatecakecodes.bluebeats.mpv.mpvevents

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import mpv.mpv_event
import mpv.mpv_event_hook
import mpv.mpv_event_property

@ExperimentalForeignApi
internal interface EventConsumer {

    val requestedEvents: Set<UInt>
        get() = emptySet()
    val requestedHooks: Set<String>
        get() = emptySet()
    val requestedProperties: Set<Pair<String, mpv.mpv_format>>
        get() = emptySet()

    suspend fun onEvent(eventId: UInt, event: CPointer<mpv_event>) {}

    suspend fun onHook(hookId: String, event:  CPointer<mpv_event_hook>) {}

    suspend fun onProperty(propertyId: String, event: CPointer<mpv_event_property>) {}

    /**
     * called when the event-loop is stopping;
     * during and after this call EventHandler::registerEventConsumer() and EventHandler::deregisterEventConsumer() must not be called
     */
    suspend fun terminate() {}
}
