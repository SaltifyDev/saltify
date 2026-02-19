package org.ntqqrev.milky.core

import io.ktor.client.plugins.websocket.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.ntqqrev.milky.Event
import org.ntqqrev.milky.dsl.MilkyClientConfig

public class MilkyClientWebSocket(config: MilkyClientConfig) : MilkyClient(config) {
    private var connectionJob: Job? = null

    override suspend fun connectEvent() {
        connectionJob?.cancelAndJoin()

        connectionJob = clientScope.launch {
            val urlString = "$addressBaseNormalized/event".replace("http", "ws")

            httpClient.webSocket(urlString) {
                while (isActive) {
                    val event = receiveDeserialized<Event>()
                    events.emit(event)
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
