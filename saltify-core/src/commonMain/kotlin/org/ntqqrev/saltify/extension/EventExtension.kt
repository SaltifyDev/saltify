package org.ntqqrev.saltify.extension

import org.ntqqrev.milky.Event
import org.ntqqrev.milky.IncomingMessage
import org.ntqqrev.milky.OutgoingSegment
import org.ntqqrev.saltify.core.SaltifyApplication
import org.ntqqrev.saltify.core.sendGroupMessage
import org.ntqqrev.saltify.core.sendPrivateMessage
import org.ntqqrev.saltify.dsl.SaltifyPluginBuilder

/**
 * 响应事件。鉴于 Context Parameter 尚未完善，这里需要手动传 client。建议使用 [SaltifyPluginBuilder.respond]。
 */
public suspend fun Event.MessageReceive.respond(
    client: SaltifyApplication,
    block: MutableList<OutgoingSegment>.() -> Unit
): Any = when (val data = this.data) {
    is IncomingMessage.Group -> client.sendGroupMessage(data.peerId, block)
    else -> client.sendPrivateMessage(data.peerId, block)
}
