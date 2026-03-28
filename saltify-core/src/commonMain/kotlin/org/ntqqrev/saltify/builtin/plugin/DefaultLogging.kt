package org.ntqqrev.saltify.builtin.plugin

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.ntqqrev.milky.Event
import org.ntqqrev.milky.IncomingMessage
import org.ntqqrev.saltify.dsl.SaltifyPlugin
import org.ntqqrev.saltify.extension.plainText
import org.ntqqrev.saltify.model.EventConnectionState
import org.ntqqrev.saltify.model.SaltifyComponentType
import org.ntqqrev.saltify.util.coroutine.saltifyComponent

/**
 * Saltify 内置日志插件。
 *
 * 负责输出事件服务连接状态变更、未捕获异常以及收到的消息日志。
 */
public val defaultLogging: SaltifyPlugin<Unit> = SaltifyPlugin("default-logging") {
    // 事件服务连接状态日志
    launch {
        client.eventConnectionStateFlow.collect { state ->
            when (state) {
                is EventConnectionState.Connected ->
                    client.logger.info("事件服务已连接, 使用协议：${state.type.name}")
                is EventConnectionState.Disconnected -> {
                    val error = state.throwable
                    if (error != null && error !is CancellationException)
                        client.logger.error("事件服务已断开", error)
                }
                is EventConnectionState.Connecting ->
                    client.logger.info("事件服务正在连接...")
                is EventConnectionState.Reconnecting ->
                    client.logger.warn(
                        "事件服务断开, 将在 ${state.delay}ms 后尝试重连... (重试次数: ${state.attempt})",
                        state.throwable
                    )
            }
        }
    }

    // 未捕获异常日志
    launch {
        client.exceptionFlow.collect { (context, throwable) ->
            val component = context.saltifyComponent!!
            when (component.type) {
                SaltifyComponentType.Application ->
                    client.logger.error("Saltify 根组件异常", throwable)
                SaltifyComponentType.Plugin ->
                    client.logger.error("Saltify 插件 ${component.name} 异常", throwable)
                SaltifyComponentType.Extension ->
                    client.logger.error("Saltify 基础扩展组件异常", throwable)
            }
        }
    }

    // 收到消息日志
    on<Event.MessageReceive> { event ->
        when (val data = event.data) {
            is IncomingMessage.Group ->
                logger.debug(
                    "${data.groupMember.userId}(${data.group.groupId}): ${event.segments.plainText}"
                )
            else ->
                logger.debug("${event.peerId}: ${event.segments.plainText}")
        }
    }
}
