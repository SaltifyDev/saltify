package org.ntqqrev.saltify.extension

import org.ntqqrev.milky.Event
import org.ntqqrev.milky.IncomingMessage
import org.ntqqrev.milky.OutgoingSegment
import org.ntqqrev.saltify.core.SaltifyApplication
import org.ntqqrev.saltify.core.sendGroupMessage
import org.ntqqrev.saltify.core.sendPrivateMessage

public suspend fun Event.MessageReceive.respond(
    client: SaltifyApplication,
    block: MutableList<OutgoingSegment>.() -> Unit
): Any = when (val data = this.data) {
    is IncomingMessage.Group -> client.sendGroupMessage(data.peerId, block)
    else -> client.sendPrivateMessage(data.peerId, block)
}
