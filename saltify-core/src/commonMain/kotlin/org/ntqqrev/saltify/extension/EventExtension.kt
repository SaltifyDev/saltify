package org.ntqqrev.saltify.extension

import org.ntqqrev.milky.Event
import org.ntqqrev.milky.IncomingMessage
import org.ntqqrev.milky.OutgoingSegment
import org.ntqqrev.saltify.core.SaltifyApplication
import org.ntqqrev.saltify.core.sendGroupMessage
import org.ntqqrev.saltify.core.sendPrivateMessage
import org.ntqqrev.saltify.dsl.SaltifyPluginContext
import org.ntqqrev.saltify.model.milky.SendMessageOutput

/**
 * 响应事件。鉴于 Context Parameter 尚未完善，这里需要手动传 client。建议使用 [SaltifyPluginContext.respond]。
 */
public suspend fun Event.MessageReceive.respond(
    client: SaltifyApplication,
    block: MutableList<OutgoingSegment>.() -> Unit
): SendMessageOutput = when (val data = this.data) {
    is IncomingMessage.Group -> {
        val output = client.sendGroupMessage(data.peerId, block)
        SendMessageOutput(output.messageSeq, output.time)
    }
    else -> {
        val output = client.sendPrivateMessage(data.peerId, block)
        SendMessageOutput(output.messageSeq, output.time)
    }
}
