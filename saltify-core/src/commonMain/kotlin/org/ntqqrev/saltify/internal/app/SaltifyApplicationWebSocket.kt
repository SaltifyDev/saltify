package org.ntqqrev.saltify.internal.app

import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.webSocket
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.ntqqrev.milky.Event
import org.ntqqrev.saltify.SaltifyApplication
import org.ntqqrev.saltify.dsl.config.ApplicationConfig
import org.ntqqrev.saltify.model.event.EventConnectionState
import org.ntqqrev.saltify.model.event.EventConnectionType
import org.ntqqrev.saltify.internal.util.withRetry

internal class SaltifyApplicationWebSocket(config: ApplicationConfig) : SaltifyApplication(config) {
    private var connectionJob: Job? = null

    override suspend fun connectEvent() {
        connectionJob?.cancelAndJoin()
        eventConnectionState.emit(EventConnectionState.Connecting)

        connectionJob = applicationScope.launch {
            withRetry(
                config.connection.event.maxReconnectionAttempts,
                config.connection.event.baseReconnectionInterval,
                config.connection.event.maxReconnectionInterval,
                config.connection.event.autoReconnect,
                onRetry = { throwable, retryCount, delay ->
                    eventConnectionState.emit(EventConnectionState.Reconnecting(throwable, retryCount, delay))
                },
                onFailure = {
                    eventConnectionState.emit(EventConnectionState.Disconnected(it))
                },
                block = { resetAttempts ->
                    httpClient.webSocket(
                        "$addressBaseNormalized/event".replaceFirst("http", "ws"),
                        request = {
                            accessToken?.let { url.parameters.append("access_token", it) }
                        }
                    ) {
                        resetAttempts()

                        eventConnectionState.emit(
                            EventConnectionState.Connected(
                                EventConnectionType.WebSocket, this@SaltifyApplicationWebSocket
                            )
                        )

                        while (isActive) {
                            events.emit(receiveDeserialized<Event>())
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
