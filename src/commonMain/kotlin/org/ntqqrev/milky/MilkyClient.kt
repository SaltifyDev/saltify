package org.ntqqrev.milky

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filter
import kotlinx.serialization.json.decodeFromJsonElement

class MilkyClient(
    addressBase: String,
    val eventConnectionType: EventConnectionType = EventConnectionType.WebSocket,
    val accessToken: String? = null
) {
    val addressBaseNormalized = addressBase.trimEnd('/')
    val apiBase = "$addressBaseNormalized/api"
    val eventBase = "$addressBaseNormalized/event"

    val client = HttpClient {
        install(ContentNegotiation) {
            json(milkyJsonModule)
        }
        when (eventConnectionType) {
            EventConnectionType.WebSocket -> install(WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter(milkyJsonModule)
            }

            EventConnectionType.SSE -> install(SSE)
        }
    }

    private var webSocketSession: DefaultClientWebSocketSession? = null
    private var sseSession: SSESession? = null

    suspend fun connectEvent() = withContext(Dispatchers.IO) {
        when (eventConnectionType) {
            EventConnectionType.WebSocket -> {
                webSocketSession = client.webSocketSession(
                    urlString = eventBase.replaceFirst("http", "ws")
                ) {
                    accessToken?.let {
                        headers.append(HttpHeaders.Authorization, "Bearer $it")
                    }
                }
            }

            EventConnectionType.SSE -> {
                sseSession = client.sseSession(eventBase) {
                    accessToken?.let {
                        headers.append(HttpHeaders.Authorization, "Bearer $it")
                    }
                }
            }
        }
    }

    suspend fun subscribe(callback: suspend (Event) -> Unit) = withContext(Dispatchers.IO) {
        when (eventConnectionType) {
            EventConnectionType.WebSocket -> {
                val session = webSocketSession
                    ?: throw MilkyException(-1, "WebSocket session is not connected")
                for (frame in session.incoming) {
                    if (!isActive) break
                    if (frame is Frame.Text) {
                        val event = milkyJsonModule.decodeFromString<Event>(frame.readText())
                        callback(event)
                    }
                }
            }

            EventConnectionType.SSE -> {
                val session = sseSession
                    ?: throw MilkyException(-1, "SSE session is not connected")
                session.incoming.filter { it.event == "milky_event" }
                    .collect {
                        val event = milkyJsonModule.decodeFromString<Event>(it.data!!)
                        callback(event)
                    }
            }
        }
    }

    suspend fun disconnectEvent() = withContext(Dispatchers.IO) {
        when (eventConnectionType) {
            EventConnectionType.WebSocket -> {
                webSocketSession?.close()
                webSocketSession = null
            }

            EventConnectionType.SSE -> {
                sseSession?.cancel()
                sseSession = null
            }
        }
    }

    suspend inline fun <reified T : Any, reified R : Any> callApi(
        endpoint: ApiEndpoint<T, R>,
        param: T
    ): R = withContext(Dispatchers.IO) {
        val response = client.post(apiBase + endpoint.path) {
            accessToken?.let {
                headers.append(HttpHeaders.Authorization, "Bearer $it")
            }
            contentType(ContentType.Application.Json)
            setBody(param)
        }.body<ApiGeneralResponse>()
        if (response.retcode != 0) {
            throw MilkyException(response.retcode, response.message!!)
        }
        milkyJsonModule.decodeFromJsonElement(response.data!!)
    }

    suspend inline fun <reified R : Any> callApi(
        endpoint: ApiEndpoint<ApiEmptyStruct, R>
    ): R = callApi(endpoint, ApiEmptyStruct())
}