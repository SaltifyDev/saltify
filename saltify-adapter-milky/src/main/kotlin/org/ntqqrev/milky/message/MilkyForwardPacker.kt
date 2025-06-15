package org.ntqqrev.milky.message

import org.ntqqrev.milky.MilkyContext
import org.ntqqrev.milky.model.message.MilkyOutgoingForwardedMessageData
import org.ntqqrev.saltify.message.incoming.IncomingMessage
import org.ntqqrev.saltify.message.outgoing.ForwardFeature
import org.ntqqrev.saltify.message.outgoing.ForwardMessageBuilder

class MilkyForwardPacker(val ctx: MilkyContext) : ForwardFeature.Packer {
    override fun fake(
        uin: Long,
        name: String,
        builder: ForwardMessageBuilder.() -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun existing(incomingMessage: IncomingMessage) {
        TODO("Not yet implemented")
    }

    internal fun build(): List<MilkyOutgoingForwardedMessageData> {
        TODO("Not yet implemented")
    }
}