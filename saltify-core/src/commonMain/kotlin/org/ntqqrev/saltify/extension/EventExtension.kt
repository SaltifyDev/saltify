package org.ntqqrev.saltify.extension

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import org.ntqqrev.milky.Event
import org.ntqqrev.milky.IncomingMessage
import org.ntqqrev.milky.OutgoingSegment
import org.ntqqrev.saltify.*
import org.ntqqrev.saltify.runtime.context.EventContext
import org.ntqqrev.saltify.runtime.milky.SendMessageOutput
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * 响应事件。
 */
public suspend fun EventContext<Event.MessageReceive>.respond(
    block: MutableList<OutgoingSegment>.() -> Unit
): SendMessageOutput = when (event.data) {
    is IncomingMessage.Group -> {
        val output = client.sendGroupMessage(event.peerId, block)
        SendMessageOutput(output.messageSeq, output.time)
    }
    else -> {
        val output = client.sendPrivateMessage(event.peerId, block)
        SendMessageOutput(output.messageSeq, output.time)
    }
}

/**
 * 响应事件。这是用于返回纯文本的简写形式。
 */
public suspend inline fun EventContext<Event.MessageReceive>.respond(
    text: Any?
): SendMessageOutput = respond { text(text.toString()) }

/**
 * 响应事件，并在指定延迟后撤回消息。
 */
public suspend inline fun EventContext<Event.MessageReceive>.respondWithRecall(
    delay: Duration,
    noinline block: MutableList<OutgoingSegment>.() -> Unit
) {
    val output = respond(block)
    delay(delay)
    when (val data = event.data) {
        is IncomingMessage.Group -> client.recallGroupMessage(data.peerId, output.messageSeq)
        else -> client.recallPrivateMessage(data.peerId, output.messageSeq)
    }
}

/**
 * 响应事件，并在指定延迟后撤回消息。这是用于返回纯文本的简写形式。
 */
public suspend inline fun EventContext<Event.MessageReceive>.respondWithRecall(
    delay: Duration,
    text: Any?
): Unit = respondWithRecall(delay) { text(text.toString()) }

/**
 * 获取由事件触发者发送的下一条消息事件。超时返回 null。
 */
public suspend fun EventContext<Event.MessageReceive>.awaitNextMessage(
    timeout: Duration = 30.seconds
): Event.MessageReceive? {
    val messageFlow = client.eventFlow.filterIsInstance<Event.MessageReceive>()

    return withTimeoutOrNull(timeout) {
        messageFlow.first { nextEvent ->
            when (val contextData = event.data) {
                is IncomingMessage.Group -> {
                    nextEvent.data is IncomingMessage.Group &&
                            (nextEvent.data as IncomingMessage.Group).group.groupId == contextData.group.groupId &&
                            nextEvent.senderId == event.senderId
                }
                else -> nextEvent.senderId == event.senderId
            }
        }
    }
}
