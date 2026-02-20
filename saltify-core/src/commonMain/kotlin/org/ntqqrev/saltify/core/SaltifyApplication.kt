package org.ntqqrev.saltify.core

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
import org.ntqqrev.saltify.annotation.WithApiExtension
import org.ntqqrev.saltify.dsl.SaltifyConfig
import org.ntqqrev.saltify.dsl.SaltifyPluginBuilder
import org.ntqqrev.saltify.entity.EventConnectionType
import org.ntqqrev.saltify.exception.ApiCallException
import kotlin.coroutines.CoroutineContext

@WithApiExtension
public sealed class SaltifyApplication(private val config: SaltifyConfig) : AutoCloseable {
    public companion object {
        public operator fun invoke(block: SaltifyConfig.() -> Unit): SaltifyApplication {
            val config = SaltifyConfig().apply(block)
            return when (config.eventConnectionType) {
                EventConnectionType.WebSocket -> SaltifyApplicationWebSocket(config)
                EventConnectionType.SSE -> SaltifyApplicationSSE(config)
            }
        }
    }

    private val exceptionHandlerProvider = SaltifyExceptionHandlerProvider()

    public val exceptionFlow: SharedFlow<Pair<CoroutineContext, Throwable>> =
        exceptionHandlerProvider.exceptionFlow.asSharedFlow()

    @PublishedApi
    internal val clientScope: CoroutineScope = CoroutineScope(
        SupervisorJob() + exceptionHandlerProvider.handler
    )

    protected val addressBaseNormalized: String = config.addressBase.trimEnd('/')

    protected val events: MutableSharedFlow<Event> = MutableSharedFlow(extraBufferCapacity = 64)
    public val eventFlow: SharedFlow<Event> = events.asSharedFlow()

    private val activePlugins = mutableListOf<SaltifyPluginBuilder>()

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
            val pluginScope = CoroutineScope(
                clientScope.coroutineContext +
                        SupervisorJob(clientScope.coroutineContext.job) +
                        CoroutineName("SaltifyPlugin-${plugin.name}") +
                        exceptionHandlerProvider.handler
            )

            val context = SaltifyPluginBuilder(this, pluginScope)
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

        if (response.retcode != 0) throw ApiCallException(response.retcode, response.message ?: "Unknown error")
        return milkyJsonModule.decodeFromJsonElement(response.data!!)
    }

    public suspend inline fun <reified R : Any> callApi(
        endpoint: ApiEndpoint<ApiEmptyStruct, R>
    ): R = callApi(endpoint, ApiEmptyStruct())

    public open suspend fun connectEvent() {
        activePlugins.forEach { context ->
            context.launch {
                context.onStartHooks.forEach { it() }
            }.join()
        }
    }

    public open suspend fun disconnectEvent() {
        activePlugins.forEach { context ->
            context.launch {
                context.onStopHooks.forEach { it() }
            }.join()
        }
    }

    override fun close() {
        httpClient.close()
        clientScope.cancel()
    }
}
