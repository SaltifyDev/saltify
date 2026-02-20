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
import org.ntqqrev.saltify.entity.SaltifyComponentType
import org.ntqqrev.saltify.exception.ApiCallException
import org.ntqqrev.saltify.util.coroutine.SaltifyComponent
import org.ntqqrev.saltify.util.coroutine.SaltifyExceptionHandlerProvider
import kotlin.coroutines.CoroutineContext

/**
 * 一个 Saltify 应用实例
 */
@WithApiExtension
public sealed class SaltifyApplication(private val config: SaltifyConfig) : AutoCloseable {
    public companion object {
        /**
         * 创建一个 Saltify 应用实例。
         */
        public operator fun invoke(block: SaltifyConfig.() -> Unit): SaltifyApplication {
            val config = SaltifyConfig().apply(block)
            return when (config.eventConnectionType) {
                EventConnectionType.WebSocket -> SaltifyApplicationWebSocket(config)
                EventConnectionType.SSE -> SaltifyApplicationSSE(config)
            }
        }
    }

    internal val exceptionHandlerProvider = SaltifyExceptionHandlerProvider()

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

    internal val applicationScope: CoroutineScope = CoroutineScope(
        SaltifyComponent(SaltifyComponentType.Application, "SaltifyApplication") +
            exceptionHandlerProvider.handler
    )

    @PublishedApi
    internal val extensionScope: CoroutineScope = CoroutineScope(
        applicationScope.coroutineContext +
            SupervisorJob(applicationScope.coroutineContext.job) +
            SaltifyComponent(SaltifyComponentType.Extension, "SaltifyExtension")
    )

    protected val addressBaseNormalized: String = config.addressBase.trimEnd('/')

    protected val events: MutableSharedFlow<Event> = MutableSharedFlow(extraBufferCapacity = 64)

    /**
     * 事件流。
     *
     * 通常不需要手动操作这个流，因为已经提供了一些高层 API。
     */
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
                applicationScope.coroutineContext +
                    SupervisorJob(applicationScope.coroutineContext.job) +
                    SaltifyComponent(SaltifyComponentType.Plugin, plugin.name)
            )

            val context = SaltifyPluginBuilder(this, pluginScope)
            plugin.setup(context)

            activePlugins.add(context)
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
    public open suspend fun connectEvent(): Unit = coroutineScope {
        activePlugins.map { context ->
            context.launch {
                context.onStartHooks.forEach { it() }
            }
        }.joinAll()
    }

    /**
     * 断开事件服务。请搭配 [connectEvent] 使用。
     */
    public open suspend fun disconnectEvent(): Unit = coroutineScope {
        activePlugins.map { context ->
            context.launch {
                context.onStopHooks.forEach { it() }
            }
        }.joinAll()
    }

    override fun close() {
        httpClient.close()
        applicationScope.cancel()
    }
}
