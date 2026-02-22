package org.ntqqrev.saltify.core

import io.ktor.client.plugins.websocket.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.ntqqrev.milky.Event
import org.ntqqrev.saltify.dsl.config.SaltifyApplicationConfig
import org.ntqqrev.saltify.model.EventConnectionState
import org.ntqqrev.saltify.model.EventConnectionType
import org.ntqqrev.saltify.util.coroutine.withRetry

public class SaltifyApplicationWebSocket(config: SaltifyApplicationConfig) : SaltifyApplication(config) {
    private var connectionJob: Job? = null

    override suspend fun connectEvent() {
        connectionJob?.cancelAndJoin()
        eventConnectionState.emit(EventConnectionState.Connecting)

        connectionJob = applicationScope.launch {
            val urlString = "$addressBaseNormalized/event".replaceFirst("http", "ws")

            withRetry(
                config.eventConnectionConfig.maxReconnectionAttempts,
                config.eventConnectionConfig.baseReconnectionInterval,
                config.eventConnectionConfig.maxReconnectionInterval,
                config.eventConnectionConfig.autoReconnect,
                onRetry = { throwable, retryCount ->
                    eventConnectionState.emit(EventConnectionState.Reconnecting(throwable, retryCount))
                },
                onFailure = {
                    eventConnectionState.emit(EventConnectionState.Disconnected(it))
                },
                block = {
                    httpClient.webSocket(urlString) {
                        eventConnectionState.emit(
                            EventConnectionState.Connected(
                                EventConnectionType.WebSocket, this@SaltifyApplicationWebSocket
                            )
                        )

                        while (isActive) {
                            val event = receiveDeserialized<Event>()
                            events.emit(event)
                        }
                    }
                }
            )
        }
    }

    override suspend fun disconnectEvent() {
        connectionJob?.cancelAndJoin()
    }
}
