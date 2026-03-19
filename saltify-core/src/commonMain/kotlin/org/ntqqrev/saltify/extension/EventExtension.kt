package org.ntqqrev.saltify.extension

import org.ntqqrev.milky.Event
import org.ntqqrev.milky.IncomingMessage
import org.ntqqrev.milky.OutgoingSegment
import org.ntqqrev.saltify.core.SaltifyApplication
import org.ntqqrev.saltify.core.sendGroupMessage
import org.ntqqrev.saltify.core.sendPrivateMessage
import org.ntqqrev.saltify.core.text
import org.ntqqrev.saltify.dsl.SaltifyPluginContext
import org.ntqqrev.saltify.model.milky.SendMessageOutput

/**
 * 响应事件。鉴于 Context Parameter 尚未完善，这里需要手动传 client。建议使用 [SaltifyPluginContext.respond]。
 */
public suspend fun Event.MessageReceive.respond(
    client: SaltifyApplication,
    block: MutableList<OutgoingSegment>.() -> Unit
): SendMessageOutput = when (data) {
    is IncomingMessage.Group -> {
        val output = client.sendGroupMessage(peerId, block)
        SendMessageOutput(output.messageSeq, output.time)
    }
    else -> {
        val output = client.sendPrivateMessage(peerId, block)
        SendMessageOutput(output.messageSeq, output.time)
    }
}

/**
 * 响应事件。这是用于返回纯文本的简写。鉴于 Context Parameter 尚未完善，这里需要手动传 client。
 */
public suspend inline fun Event.MessageReceive.respond(
    client: SaltifyApplication,
    text: String
): SendMessageOutput = respond(client) { text(text) }
