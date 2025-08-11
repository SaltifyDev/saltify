package org.ntqqrev.milky.message

import kotlinx.datetime.Instant
import org.ntqqrev.milky.MilkyContext
import org.ntqqrev.milky.protocol.message.*
import org.ntqqrev.milky.util.convertSegment
import org.ntqqrev.milky.util.resolveSenderName
import org.ntqqrev.milky.util.toSaltifyMessageScene
import org.ntqqrev.saltify.message.MessageScene
import org.ntqqrev.saltify.message.incoming.*

class MilkyIncomingMessage(
    override val ctx: MilkyContext,
    override val scene: MessageScene,
    override val peerUin: Long,
    override val sequence: Long,
    override val time: Instant,
    override val senderUin: Long,
    override val senderName: String,
    override val segments: List<Segment>,
) : IncomingMessage {
    companion object {
        fun fromData(ctx: MilkyContext, data: MilkyIncomingMessageData) = MilkyIncomingMessage(
            ctx,
            data.messageScene.toSaltifyMessageScene(),
            data.peerId,
            data.messageSeq,
            Instant.fromEpochSeconds(data.time),
            data.senderId,
            data.resolveSenderName(),
            data.segments.map { convertSegment(ctx, it.data) }
        )
    }
}