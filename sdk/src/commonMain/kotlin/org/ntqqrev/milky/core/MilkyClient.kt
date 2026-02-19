package org.ntqqrev.milky.core

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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.decodeFromJsonElement
import org.ntqqrev.milky.*
import org.ntqqrev.milky.annotation.WithApiExtension
import org.ntqqrev.milky.dsl.MilkyClientConfig
import org.ntqqrev.milky.dsl.MilkyPluginContext
import org.ntqqrev.milky.entity.EventConnectionType
import org.ntqqrev.milky.exception.MilkyException

@WithApiExtension
public sealed class MilkyClient(private val config: MilkyClientConfig) : AutoCloseable {
    public companion object {
        public operator fun invoke(block: MilkyClientConfig.() -> Unit): MilkyClient {
            val config = MilkyClientConfig().apply(block)
            return when (config.eventConnectionType) {
                EventConnectionType.WebSocket -> MilkyClientWebSocket(config)
                EventConnectionType.SSE -> MilkyClientSSE(config)
            }
        }
    }

    @PublishedApi
    internal val clientScope: CoroutineScope = CoroutineScope(SupervisorJob())

    protected val addressBaseNormalized: String = config.addressBase.trimEnd('/')

    protected val events: MutableSharedFlow<Event> = MutableSharedFlow(extraBufferCapacity = 64)
    public val eventFlow: SharedFlow<Event> = events.asSharedFlow()

    private val activePlugins = mutableListOf<MilkyPluginContext>()

    @PublishedApi
    internal val httpClient: HttpClient = HttpClient {
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

    init {
        config.installedPlugins.forEach { plugin ->
            val pluginJob = SupervisorJob(clientScope.coroutineContext.job)
            val pluginScope = CoroutineScope(
                clientScope.coroutineContext + pluginJob + CoroutineName("MilkyPlugin-${plugin.name}")
            )

            val context = MilkyPluginContext(this, pluginScope)
            plugin.setup(context)

            activePlugins.add(context)
        }
    }

    public suspend inline fun <reified T : Any, reified R : Any> callApi(
        endpoint: ApiEndpoint<T, R>,
        param: T
    ): R {
        val response: ApiGeneralResponse = httpClient.post("/api${endpoint.path}") {
            contentType(ContentType.Application.Json)
            setBody(param)
        }.body()

        if (response.retcode != 0) throw MilkyException(response.retcode, response.message ?: "Unknown error")
        return milkyJsonModule.decodeFromJsonElement(response.data!!)
    }

    public suspend inline fun <reified R : Any> callApi(
        endpoint: ApiEndpoint<ApiEmptyStruct, R>
    ): R = callApi(endpoint, ApiEmptyStruct())

    public open suspend fun connectEvent() {
        activePlugins.forEach { context ->
            context.launch {
                context.onStartHooks.forEach { it() }
            }
        }
    }

    public open suspend fun disconnectEvent() {
        activePlugins.forEach { context ->
            context.launch {
                context.onStopHooks.forEach { it() }
            }
        }
    }

    override fun close() {
        httpClient.close()
        clientScope.cancel()
    }
}
