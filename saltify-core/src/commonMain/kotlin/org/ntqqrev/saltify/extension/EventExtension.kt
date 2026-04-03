package org.ntqqrev.saltify.extension

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import org.ntqqrev.milky.Event
import org.ntqqrev.milky.IncomingMessage
import org.ntqqrev.milky.OutgoingSegment
import org.ntqqrev.saltify.core.recallGroupMessage
import org.ntqqrev.saltify.core.recallPrivateMessage
import org.ntqqrev.saltify.core.sendGroupMessage
import org.ntqqrev.saltify.core.sendPrivateMessage
import org.ntqqrev.saltify.core.text
import org.ntqqrev.saltify.entity.env.EventEnvironment
import org.ntqqrev.saltify.model.milky.SendMessageOutput
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * 响应事件。
 */
context(ctx: EventEnvironment<Event.MessageReceive>)
public suspend fun respond(
    block: MutableList<OutgoingSegment>.() -> Unit
): SendMessageOutput = when (ctx.event.data) {
    is IncomingMessage.Group -> {
        val output = ctx.client.sendGroupMessage(ctx.event.peerId, block)
        SendMessageOutput(output.messageSeq, output.time)
    }
    else -> {
        val output = ctx.client.sendPrivateMessage(ctx.event.peerId, block)
        SendMessageOutput(output.messageSeq, output.time)
    }
}

/**
 * 响应事件。这是用于返回纯文本的简写形式。
 */
context(_: EventEnvironment<Event.MessageReceive>)
public suspend inline fun respond(
    text: Any?
): SendMessageOutput = respond { text(text.toString()) }

/**
 * 响应事件，并在指定延迟后撤回消息。
 */
context(ctx: EventEnvironment<Event.MessageReceive>)
public suspend inline fun respondWithRecall(
    delay: Duration,
    noinline block: MutableList<OutgoingSegment>.() -> Unit
) {
    val output = respond(block)
    delay(delay)
    when (val data = ctx.event.data) {
        is IncomingMessage.Group -> ctx.client.recallGroupMessage(data.peerId, output.messageSeq)
        else -> ctx.client.recallPrivateMessage(data.peerId, output.messageSeq)
    }
}

/**
 * 响应事件，并在指定延迟后撤回消息。这是用于返回纯文本的简写形式。
 */
context(_: EventEnvironment<Event.MessageReceive>)
public suspend inline fun respondWithRecall(
    delay: Duration,
    text: Any?
): Unit = respondWithRecall(delay) { text(text.toString()) }

/**
 * 获取由事件触发者发送的下一条消息事件。超时返回 null。
 */
context(ctx: EventEnvironment<Event.MessageReceive>)
public suspend fun awaitNextMessage(timeout: Duration = 30.seconds): Event.MessageReceive? {
    val messageFlow = ctx.client.eventFlow.filterIsInstance<Event.MessageReceive>()

    return withTimeoutOrNull(timeout) {
        messageFlow.first { nextEvent ->
            when (val contextData = ctx.event.data) {
                is IncomingMessage.Group -> {
                    nextEvent.data is IncomingMessage.Group &&
                            (nextEvent.data as IncomingMessage.Group).group.groupId == contextData.group.groupId &&
                            nextEvent.senderId == ctx.event.senderId
                }
                else -> nextEvent.senderId == ctx.event.senderId
            }
        }
    }
}
