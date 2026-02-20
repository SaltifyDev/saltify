package org.ntqqrev.saltify.core

import io.ktor.client.plugins.websocket.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.ntqqrev.milky.Event
import org.ntqqrev.saltify.dsl.SaltifyConfig

public class SaltifyApplicationWebSocket(config: SaltifyConfig) : SaltifyApplication(config) {
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
