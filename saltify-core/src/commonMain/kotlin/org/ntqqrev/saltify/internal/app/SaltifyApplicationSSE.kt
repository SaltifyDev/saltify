package org.ntqqrev.saltify.internal.app

import io.ktor.client.plugins.sse.sse
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import org.ntqqrev.milky.Event
import org.ntqqrev.milky.milkyJsonModule
import org.ntqqrev.saltify.SaltifyApplication
import org.ntqqrev.saltify.dsl.config.ApplicationConfig
import org.ntqqrev.saltify.model.event.EventConnectionState
import org.ntqqrev.saltify.model.event.EventConnectionType
import org.ntqqrev.saltify.internal.util.withRetry

internal class SaltifyApplicationSSE(config: ApplicationConfig) : SaltifyApplication(config) {
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
                    httpClient.sse(
                        "$addressBaseNormalized/event",
                        request = {
                            accessToken?.let { url.parameters.append("access_token", it) }
                        }
                    ) {
                        resetAttempts()

                        eventConnectionState.emit(
                            EventConnectionState.Connected(
                                EventConnectionType.SSE, this@SaltifyApplicationSSE
                            )
                        )

                        incoming.collect { sseEvent ->
                            if (sseEvent.event == "milky_event") {
                                sseEvent.data?.let { data ->
                                    events.emit(milkyJsonModule.decodeFromString<Event>(data))
                                }
                            }
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
