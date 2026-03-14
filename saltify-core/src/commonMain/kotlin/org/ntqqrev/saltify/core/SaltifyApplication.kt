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
import io.ktor.util.logging.KtorSimpleLogger
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
import org.ntqqrev.saltify.dsl.SaltifyPluginContext
import org.ntqqrev.saltify.dsl.config.SaltifyApplicationConfig
import org.ntqqrev.saltify.entity.InstalledPlugin
import org.ntqqrev.saltify.exception.ApiCallException
import org.ntqqrev.saltify.extension.plainText
import org.ntqqrev.saltify.model.EventConnectionState
import org.ntqqrev.saltify.model.EventConnectionType
import org.ntqqrev.saltify.model.SaltifyComponentType
import org.ntqqrev.saltify.util.coroutine.SaltifyComponent
import org.ntqqrev.saltify.util.coroutine.SaltifyExceptionHandlerProvider
import org.ntqqrev.saltify.util.coroutine.saltifyComponent
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock

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
            return when (config.connection.event.type) {
                EventConnectionType.WebSocket -> SaltifyApplicationWebSocket(config)
                EventConnectionType.SSE -> SaltifyApplicationSSE(config)
            }
        }
    }

    internal val logger = KtorSimpleLogger("Saltify/main")

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

    protected val addressBaseNormalized: String = config.connection.baseUrl.trimEnd('/')

    private val loadedPlugins = mutableListOf<SaltifyPluginContext>()

    @PublishedApi
    internal val httpClient: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(milkyJsonModule)
        }

        defaultRequest {
            url(addressBaseNormalized)
            config.connection.accessToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
        }

        when (config.connection.event.type) {
            EventConnectionType.WebSocket -> install(WebSockets.Plugin) {
                contentConverter = KotlinxWebsocketSerializationConverter(milkyJsonModule)
            }

            EventConnectionType.SSE -> install(SSE)
        }
    }

    private lateinit var loggingListenerJob: Job

    public suspend fun start(): SaltifyApplication {
        logger.info("Saltify 正在启动...")
        val startInstant = Clock.System.now()

        loggingListenerJob = startLoggingListeners()

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

            val context = SaltifyPluginContext(plugin.name, this, pluginScope)

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
        loggingListenerJob.cancel()
        httpClient.close()
        applicationScope.cancel()
    }
}

private fun SaltifyApplication.startLoggingListeners() = applicationScope.launch {
    // 事件服务状态日志
    applicationScope.launch {
        eventConnectionStateFlow.collect {
            when (it) {
                is EventConnectionState.Connected ->
                    logger.info("事件服务已连接, 使用协议：${it.type.name}")
                is EventConnectionState.Disconnected -> {
                    val error = it.throwable
                    if (error != null && error !is CancellationException) logger.error("事件服务已断开", error)
                }
                is EventConnectionState.Connecting ->
                    logger.info("事件服务正在连接...")
                is EventConnectionState.Reconnecting ->
                    logger.warn("事件服务断开, 将在 ${it.delay}ms 后尝试重连... (重试次数: ${it.attempt})", it.throwable)
            }
        }
    }

    // 错误日志
    applicationScope.launch {
        exceptionFlow.collect {
            val component = it.first.saltifyComponent!!
            when (component.type) {
                SaltifyComponentType.Application ->
                    logger.error("Saltify 根组件异常", it.second)
                SaltifyComponentType.Plugin ->
                    logger.error("Saltify 插件 ${component.name} 异常", it.second)
                SaltifyComponentType.Extension ->
                    logger.error("Saltify 基础扩展组件异常", it.second)
            }
        }
    }

    // 事件日志
    applicationScope.launch {
        eventFlow.collect {
            when (it) {
                is Event.MessageReceive -> {
                    when (val data = it.data) {
                        is IncomingMessage.Group ->
                            logger.debug(
                                "${data.groupMember.userId}(${data.group.groupId}): ${it.segments.plainText}"
                            )
                        else ->
                            logger.debug("${it.peerId}: ${it.segments.plainText}")
                    }
                }
                else -> {}
            }
        }
    }
}
