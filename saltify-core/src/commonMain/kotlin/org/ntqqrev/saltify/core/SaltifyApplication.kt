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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.decodeFromJsonElement
import org.ntqqrev.milky.*
import org.ntqqrev.saltify.annotation.WithApiExtension
import org.ntqqrev.saltify.dsl.config.SaltifyApplicationConfig
import org.ntqqrev.saltify.dsl.SaltifyPluginContext
import org.ntqqrev.saltify.exception.ApiCallException
import org.ntqqrev.saltify.model.EventConnectionState
import org.ntqqrev.saltify.model.EventConnectionType
import org.ntqqrev.saltify.model.SaltifyComponentType
import org.ntqqrev.saltify.util.coroutine.SaltifyComponent
import org.ntqqrev.saltify.util.coroutine.SaltifyExceptionHandlerProvider
import kotlin.coroutines.CoroutineContext

/**
 * 一个 Saltify 应用实例
 */
@WithApiExtension
public sealed class SaltifyApplication(protected val config: SaltifyApplicationConfig) : AutoCloseable {
    public companion object {
        /**
         * 创建一个 Saltify 应用实例。
         */
        public operator fun invoke(block: SaltifyApplicationConfig.() -> Unit): SaltifyApplication {
            val config = SaltifyApplicationConfig().apply(block)
            return when (config.eventConnectionConfig.type) {
                EventConnectionType.WebSocket -> SaltifyApplicationWebSocket(config)
                EventConnectionType.SSE -> SaltifyApplicationSSE(config)
            }
        }
    }

    @PublishedApi
    internal val exceptionHandlerProvider: SaltifyExceptionHandlerProvider = SaltifyExceptionHandlerProvider()

    /**
     * 全局异常流。
     *
     * 可以通过 [SaltifyComponent] 判断异常抛出位置。
     *
     * ```kotlin
     * client.exceptionFlow.collect { (context, exception) ->
     *     val component = context.saltifyComponent!!
     *     println("Component ${component.name}(${component.type}) occurred an exception: $exception")
     * }
     * ```
     */
    public val exceptionFlow: SharedFlow<Pair<CoroutineContext, Throwable>> =
        exceptionHandlerProvider.exceptionFlow.asSharedFlow()

    protected val eventConnectionState: MutableStateFlow<EventConnectionState> =
        MutableStateFlow(EventConnectionState.Disconnected(null))

    /**
     * 事件服务连接状态流。
     */
    public val eventConnectionStateFlow: StateFlow<EventConnectionState> = eventConnectionState.asStateFlow()

    internal val applicationScope: CoroutineScope = CoroutineScope(
        SaltifyComponent(SaltifyComponentType.Application, "SaltifyApplication") +
            exceptionHandlerProvider.handler
    )

    protected val events: MutableSharedFlow<Event> = MutableSharedFlow(extraBufferCapacity = 64)

    /**
     * 事件流。
     *
     * 通常不需要手动操作这个流，因为已经提供了一些高层 API。
     */
    public val eventFlow: SharedFlow<Event> = events.asSharedFlow()

    @PublishedApi
    internal val extensionScope: CoroutineScope = CoroutineScope(
        applicationScope.coroutineContext +
            SupervisorJob(applicationScope.coroutineContext.job) +
            SaltifyComponent(SaltifyComponentType.Extension, "SaltifyExtension")
    )

    protected val addressBaseNormalized: String = config.addressBase.trimEnd('/')

    private val loadedPlugins = mutableListOf<SaltifyPluginContext>()

    @PublishedApi
    internal val httpClient: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(milkyJsonModule)
        }

        defaultRequest {
            url(addressBaseNormalized)
            config.accessToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
        }

        when (config.eventConnectionConfig.type) {
            EventConnectionType.WebSocket -> install(WebSockets.Plugin) {
                contentConverter = KotlinxWebsocketSerializationConverter(milkyJsonModule)
            }

            EventConnectionType.SSE -> install(SSE)
        }
    }

    init {
        config.installedPlugins.forEach { plugin ->
            val pluginScope = CoroutineScope(
                applicationScope.coroutineContext +
                    SupervisorJob(applicationScope.coroutineContext.job) +
                    SaltifyComponent(SaltifyComponentType.Plugin, plugin.name)
            )

            val context = SaltifyPluginContext(this, pluginScope)
            plugin.setup(context)
            loadedPlugins.add(context)

            pluginScope.launch {
                context.onStartHooks.forEach { it() }
            }
        }
    }

    /**
     * 手动构建接口调用。不建议直接使用。
     */
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

    /**
     * 手动构建接口调用。不建议直接使用。
     */
    public suspend inline fun <reified R : Any> callApi(
        endpoint: ApiEndpoint<ApiEmptyStruct, R>
    ): R = callApi(endpoint, ApiEmptyStruct())

    /**
     * 连接事件服务。需要在监听事件时调用。请搭配 [disconnectEvent] 使用。
     */
    public abstract suspend fun connectEvent()

    /**
     * 断开事件服务。请搭配 [connectEvent] 使用。
     */
    public abstract suspend fun disconnectEvent()

    override fun close() {
        loadedPlugins.forEach {
            it.onStopHooks.forEach { block -> block() }
        }
        httpClient.close()
        applicationScope.cancel()
    }
}
