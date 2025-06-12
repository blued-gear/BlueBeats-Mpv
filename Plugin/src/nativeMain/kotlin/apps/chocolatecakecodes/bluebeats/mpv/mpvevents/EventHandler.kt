package apps.chocolatecakecodes.bluebeats.mpv.mpvevents

import apps.chocolatecakecodes.bluebeats.mpv.PluginContext
import apps.chocolatecakecodes.bluebeats.mpv.utils.Logger
import apps.chocolatecakecodes.bluebeats.mpv.utils.checkReturnCode
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKString
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mpv.mpv_event
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalForeignApi::class, ExperimentalAtomicApi::class)
internal class EventHandler(
    private val pluginContext: PluginContext,
) {

    private var running = AtomicBoolean(false)
    private val consumersMutex = Mutex()
    private val consumers: MutableList<EventConsumer> = mutableListOf()
    private val consumersByEvent: MutableMap<UInt, MutableList<EventConsumer>> = mutableMapOf()
    private val consumersByHook: MutableMap<String, MutableList<EventConsumer>> = mutableMapOf()
    private val consumersByProperty: MutableMap<String, MutableList<EventConsumer>> = mutableMapOf()

    init {
        mpv.mpv_request_event(pluginContext.mpvCtx, mpv.MPV_EVENT_SHUTDOWN, 1)
            .checkReturnCode("register event MPV_EVENT_SHUTDOWN")
    }

    suspend fun registerEventConsumer(eventConsumer: EventConsumer) {
        consumersMutex.withLock {
            consumers.add(eventConsumer)
            eventConsumer.requestedEvents.forEach { event ->
                consumersByEvent.getOrPut(event) {
                    mpv.mpv_request_event(pluginContext.mpvCtx, event, 1)
                        .checkReturnCode("register event $event")
                    return@getOrPut mutableListOf()
                }.add(eventConsumer)
            }
            eventConsumer.requestedHooks.forEach { hook ->
                consumersByHook.getOrPut(hook) {
                    mpv.mpv_hook_add(pluginContext.mpvCtx, 0UL, hook, 1)
                        .checkReturnCode("register hook $hook")
                    return@getOrPut mutableListOf()
                }.add(eventConsumer)
            }
            eventConsumer.requestedProperties.forEach { (prop, format) ->
                consumersByProperty.getOrPut(prop) {
                    mpv.mpv_observe_property(pluginContext.mpvCtx, prop.hashCode().toULong(), prop, format)
                        .checkReturnCode("observer property $prop")
                    return@getOrPut mutableListOf()
                }.add(eventConsumer)
            }
        }
    }

    suspend fun deregisterEventConsumer(eventConsumer: EventConsumer) {
        consumersMutex.withLock {
            consumers.remove(eventConsumer)
            consumersByEvent.values.forEach { it.remove(eventConsumer) }
            consumersByHook.values.forEach { it.remove(eventConsumer) }

            eventConsumer.requestedProperties.forEach { (prop, _) ->
                val list = consumersByProperty[prop]!!
                list.remove(eventConsumer)
                if(list.isEmpty()) {
                    consumersByProperty.remove(prop)
                    mpv.mpv_unobserve_property(pluginContext.mpvCtx, prop.hashCode().toULong())
                        .checkReturnCode("unobserve property $prop")
                }
            }
        }
    }

    suspend fun runLoop() {
        running.store(true)
        coroutineScope {
            launch {
                while(running.load())
                    processIncomingEvent()
            }.join()
        }
    }

    suspend fun stopLoop() {
        running.store(false)

        consumersMutex.withLock {
            consumers.forEach { consumer ->
                try {
                    consumer.terminate()
                } catch(e: Throwable) {
                    Logger.error("EventHandler", "uncaught exception in EventConsumer.terminate()", e)
                }
            }
        }

        currentCoroutineContext().cancelChildren(CancellationException("shutdown"))
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun processIncomingEvent() {
        val event = withContext(Dispatchers.IO) {
            mpv.mpv_wait_event(pluginContext.mpvCtx, -1.0)!!
        }

        when(event.pointed.event_id) {
            mpv.MPV_EVENT_SHUTDOWN -> processShutdown(event)
            mpv.MPV_EVENT_HOOK -> processHook(event)
            mpv.MPV_EVENT_PROPERTY_CHANGE -> processPropChange(event)
            else -> processEvent(event)
        }
    }

    private suspend fun processShutdown(event: CPointer<mpv_event>) {
        stopLoop()
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun processHook(event: CPointer<mpv_event>) {
        val hookEvent = event.pointed.data as CPointer<mpv.mpv_event_hook>
        val hookName = hookEvent.pointed.name!!.toKString()
        consumersMutex.withLock {
            // create copy for concurrency-safety
            consumersByHook[hookName]?.toList()
        }?.forEach { consumer ->
            try {
                consumer.onHook(hookName, hookEvent)
            } catch (e: Throwable) {
                Logger.error("EventHandler", "uncaught exception in EventConsumer.onHook()", e)
            }
        }
        mpv.mpv_hook_continue(pluginContext.mpvCtx, hookEvent.pointed.id)
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun processPropChange(event: CPointer<mpv_event>) {
        val propEvent = event.pointed.data as CPointer<mpv.mpv_event_property>
        val propName = propEvent.pointed.name!!.toKString()
        consumersMutex.withLock {
            // create copy for concurrency-safety
            consumersByProperty[propName]?.toList()
        }?.forEach { consumer ->
            try {
                consumer.onProperty(propName, propEvent)
            } catch (e: Throwable) {
                Logger.error("EventHandler", "uncaught exception in EventConsumer.onProperty()", e)
            }
        }
    }

    private suspend fun processEvent(event: CPointer<mpv_event>) {
        val eventId = event.pointed.event_id
        consumersMutex.withLock {
            // create copy for concurrency-safety
            consumersByEvent[eventId]?.toList()
        }?.forEach { consumer ->
            try {
                consumer.onEvent(eventId, event)
            } catch (e: Throwable) {
                Logger.error("EventHandler", "uncaught exception in EventConsumer.onEvent()", e)
            }
        }
    }
}
