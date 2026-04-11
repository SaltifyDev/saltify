package org.ntqqrev.saltify

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
import io.ktor.util.logging.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.decodeFromJsonElement
import org.ntqqrev.milky.*
import org.ntqqrev.saltify.annotation.WithApiExtension
import org.ntqqrev.saltify.dsl.PluginBuilder
import org.ntqqrev.saltify.dsl.config.ApplicationConfig
import org.ntqqrev.saltify.exception.ApiCallException
import org.ntqqrev.saltify.internal.app.SaltifyApplicationSSE
import org.ntqqrev.saltify.internal.app.SaltifyApplicationWebSocket
import org.ntqqrev.saltify.internal.util.ExceptionHandlerProvider
import org.ntqqrev.saltify.internal.util.InstalledPlugin
import org.ntqqrev.saltify.model.SaltifyComponentType
import org.ntqqrev.saltify.model.event.EventConnectionState
import org.ntqqrev.saltify.model.event.EventConnectionType
import org.ntqqrev.saltify.runtime.SaltifyComponent
import org.ntqqrev.saltify.runtime.command.RegisteredCommand
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock

/**
 * 一个 Saltify 应用实例
 */
@WithApiExtension
public abstract class SaltifyApplication internal constructor(
    @PublishedApi internal val config: ApplicationConfig
) : AutoCloseable {
    public companion object {
        /**
         * 创建一个 Saltify 应用实例。
         */
        public operator fun invoke(block: ApplicationConfig.() -> Unit): SaltifyApplication {
            val config = ApplicationConfig().apply(block)
            return when (config.connection.event.type) {
                EventConnectionType.WebSocket -> SaltifyApplicationWebSocket(config)
                EventConnectionType.SSE -> SaltifyApplicationSSE(config)
            }
        }
    }

    internal val logger = KtorSimpleLogger("Saltify/main")

    @PublishedApi
    internal val exceptionHandlerProvider: ExceptionHandlerProvider = ExceptionHandlerProvider()

    /**
     * 全局异常流。
     *
     * 可以通过 [org.ntqqrev.saltify.runtime.SaltifyComponent] 判断异常抛出位置。
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

    protected val addressBaseNormalized: String = config.connection.baseUrl.trimEnd('/')

    private val loadedPlugins = mutableListOf<PluginBuilder>()

    internal val commandRegistry: MutableList<RegisteredCommand> = mutableListOf()

    @PublishedApi
    internal val accessToken: String? = config.connection.accessToken

    @PublishedApi
    internal val httpClient: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(milkyJsonModule)
        }

        defaultRequest {
            url(addressBaseNormalized)
        }

        when (config.connection.event.type) {
            EventConnectionType.WebSocket -> install(WebSockets.Plugin) {
                contentConverter = KotlinxWebsocketSerializationConverter(milkyJsonModule)
            }

            EventConnectionType.SSE -> install(SSE)
        }
    }

    public suspend fun start(): SaltifyApplication {
        logger.info("Saltify 正在启动...")
        val startInstant = Clock.System.now()

        // 插件初始化
        config.installedPlugins.map { installed ->
            @Suppress("UNCHECKED_CAST")
            val installed = installed as InstalledPlugin<Any>
            val plugin = installed.plugin

            val configInstance = plugin.createConfig().apply(installed.configure)

            val pluginScope = CoroutineScope(
                applicationScope.coroutineContext +
                        SupervisorJob(applicationScope.coroutineContext.job) +
                        SaltifyComponent(SaltifyComponentType.Plugin, plugin.name)
            )

            val context = PluginBuilder(this, pluginScope, plugin.name)

            plugin.setup(context, configInstance)
            loadedPlugins.add(context)

            pluginScope.launch {
                context.onStartHooks.forEach { it() }
            }
        }.joinAll()

        logger.info("Saltify 初始化完毕, 用时 ${Clock.System.now() - startInstant}")
        return this
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
            accessToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }

            timeout {
                requestTimeoutMillis = config.connection.apiRequestTimeout
            }
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

        logger.info("Saltify 已关闭")
    }
}
