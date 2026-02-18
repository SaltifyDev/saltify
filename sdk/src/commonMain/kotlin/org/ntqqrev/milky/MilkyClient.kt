package org.ntqqrev.milky

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.decodeFromJsonElement

@WithApiExtension
public sealed class MilkyClient(private val config: MilkyClientConfig) {
    public companion object {
        public operator fun invoke(block: MilkyClientConfig.() -> Unit): MilkyClient {
            val config = MilkyClientConfig().apply(block)
            return when (config.eventConnectionType) {
                EventConnectionType.WebSocket -> MilkyClientWebSocket(config)
                EventConnectionType.SSE -> MilkyClientSSE(config)
            }
        }
    }

    protected val addressBaseNormalized: String = config.addressBase.trimEnd('/')

    protected val scope: CoroutineScope = CoroutineScope(SupervisorJob())

    protected val events: MutableSharedFlow<Event> = MutableSharedFlow(extraBufferCapacity = 64)
    public val eventFlow: SharedFlow<Event> = events.asSharedFlow()

    @PublishedApi
    internal val client: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(milkyJsonModule)
        }

        defaultRequest {
            url(addressBaseNormalized)
            config.accessToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
        }

        when (config.eventConnectionType) {
            EventConnectionType.WebSocket -> install(WebSockets.Plugin) {
                contentConverter = KotlinxWebsocketSerializationConverter(milkyJsonModule)
            }

            EventConnectionType.SSE -> install(SSE)
        }
    }

    public suspend inline fun <reified T : Any, reified R : Any> callApi(
        endpoint: ApiEndpoint<T, R>,
        param: T
    ): R {
        val response: ApiGeneralResponse = client.post("/api${endpoint.path}") {
            contentType(ContentType.Application.Json)
            setBody(param)
        }.body()

        if (response.retcode != 0) throw MilkyException(response.retcode, response.message ?: "Unknown error")
        return milkyJsonModule.decodeFromJsonElement(response.data!!)
    }

    public suspend inline fun <reified R : Any> callApi(
        endpoint: ApiEndpoint<ApiEmptyStruct, R>
    ): R = callApi(endpoint, ApiEmptyStruct())

    public abstract suspend fun connectEvent()

    public abstract suspend fun disconnectEvent()
}

public class MilkyClientWebSocket(config: MilkyClientConfig) : MilkyClient(config) {
    private lateinit var session: DefaultClientWebSocketSession

    override suspend fun connectEvent() {
        session = client.webSocketSession(
            "$addressBaseNormalized/event".replaceFirst("http", "ws")
        )

        scope.launch {
            session.incoming.consumeAsFlow()
                .filterIsInstance<Frame.Text>()
                .mapNotNull { milkyJsonModule.decodeFromString<Event>(it.readText()) }
                .collect { events.emit(it) }
        }
    }

    override suspend fun disconnectEvent() {
        scope.cancel()
        session.close()
    }
}

public class MilkyClientSSE(config: MilkyClientConfig) : MilkyClient(config) {
    private lateinit var session: SSESession

    override suspend fun connectEvent() {
        session = client.sseSession("$addressBaseNormalized/event")

        scope.launch {
            session.incoming
                .filter { it.event == "milky_event" }
                .mapNotNull { event -> event.data?.let { milkyJsonModule.decodeFromString<Event>(it) } }
                .collect { events.emit(it) }
        }
    }

    override suspend fun disconnectEvent() {
        scope.cancel()
        session.cancel()
    }
}