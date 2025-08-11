package org.ntqqrev.milky.message

import kotlinx.datetime.Instant
import org.ntqqrev.milky.MilkyContext
import org.ntqqrev.milky.protocol.message.MilkyIncomingForwardedMessageData
import org.ntqqrev.milky.protocol.message.MilkyIncomingSegmentModel
import org.ntqqrev.milky.util.convertSegment
import org.ntqqrev.saltify.message.incoming.ForwardedIncomingMessage
import org.ntqqrev.saltify.message.incoming.Segment

class MilkyIncomingForwardedMessage(
    override val ctx: MilkyContext,
    override val senderName: String,
    override val senderAvatarLink: String,
    override val time: Instant,
    milkyIncomingData: List<MilkyIncomingSegmentModel>,
) : ForwardedIncomingMessage {
    override val segments: List<Segment> =
        milkyIncomingData.map { convertSegment(ctx, it.data) }

    companion object {
        fun fromData(
            ctx: MilkyContext,
            data: MilkyIncomingForwardedMessageData,
        ) = MilkyIncomingForwardedMessage(
            ctx,
            data.senderName,
            data.avatarUrl,
            Instant.fromEpochSeconds(data.time),
            data.segments
        )
    }
}