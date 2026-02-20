package org.ntqqrev.milky.core

import io.ktor.client.plugins.sse.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import org.ntqqrev.milky.Event
import org.ntqqrev.milky.dsl.MilkyClientConfig
import org.ntqqrev.milky.milkyJsonModule

public class MilkyClientSSE(config: MilkyClientConfig) : MilkyClient(config) {
    private var connectionJob: Job? = null

    override suspend fun connectEvent() {
        connectionJob?.cancelAndJoin()

        connectionJob = clientScope.launch {
            httpClient.sse("$addressBaseNormalized/event") {
                incoming.collect { sseEvent ->
                    if (sseEvent.event == "milky_event") {
                        sseEvent.data?.let { data ->
                            val event = milkyJsonModule.decodeFromString<Event>(data)
                            events.emit(event)
                        }
                    }
                }
            }
        }

        super.connectEvent()
    }

    override suspend fun disconnectEvent() {
        connectionJob?.cancelAndJoin()
        super.disconnectEvent()
    }
}
