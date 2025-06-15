package org.ntqqrev.milky.message

import kotlinx.datetime.Instant
import org.ntqqrev.milky.MilkyContext
import org.ntqqrev.milky.model.message.*
import org.ntqqrev.milky.util.toImageSubType
import org.ntqqrev.saltify.Context
import org.ntqqrev.saltify.message.ImageSubType
import org.ntqqrev.saltify.message.incoming.*
import org.ntqqrev.saltify.model.Group
import org.ntqqrev.saltify.model.GroupMember
import org.ntqqrev.saltify.model.User

internal fun convertSegment(message: IncomingMessage, data: MilkyIncomingData) =
    when (data) {
        is MilkyIncomingTextData -> TextSegment(message, data.text)
        is MilkyIncomingMentionData -> MentionSegment(message, data.userId)
        is MilkyIncomingMentionAllData -> MentionSegment(message, null)
        is MilkyIncomingFaceData -> FaceSegment(message, data.faceId)
        is MilkyIncomingReplyData -> ReplySegment(message, data.messageSeq)
        is MilkyIncomingImageData -> ImageSegment(
            message,
            data.resourceId,
            data.subType?.toImageSubType() ?: ImageSubType.NORMAL,
            data.summary ?: "",
        )
        is MilkyIncomingRecordData -> RecordSegment(
            message,
            data.resourceId,
            data.duration,
        )
        is MilkyIncomingVideoData -> VideoSegment(message, data.resourceId)
        is MilkyIncomingForwardData -> ForwardSegment(message, data.forwardId)
        is MilkyIncomingMarketFaceData -> MarketFaceSegment(message, data.url)
        is MilkyIncomingLightAppData -> LightAppSegment(
            message,
            data.appName,
            data.jsonPayload
        )
        is MilkyIncomingXmlData -> XmlSegment(
            message,
            data.serviceId,
            data.xmlPayload
        )
    }

class MilkyIncomingPrivateMessage(
    override val ctx: MilkyContext,
    override val peer: User,
    override val isSelf: Boolean,
    override val sequence: Long,
    override val time: Instant,
    milkyIncomingData: List<MilkyIncomingSegmentModel>
) : PrivateIncomingMessage {
    override val segments: List<Segment> =
        milkyIncomingData.map { convertSegment(this, it.data) }

    companion object {
        suspend fun fromFriendMessage(
            ctx: MilkyContext,
            data: MilkyFriendMessageData
        ) = ctx.getFriend(data.peerId)?.let { peer ->
            MilkyIncomingPrivateMessage(
                ctx,
                peer,
                data.peerId != data.senderId,
                data.messageSeq,
                Instant.fromEpochMilliseconds(data.time),
                data.segments
            )
        }
    }
}

class MilkyIncomingGroupMessage(
    override val ctx: MilkyContext,
    override val group: Group,
    override val sender: GroupMember,
    override val sequence: Long,
    override val time: Instant,
    milkyIncomingData: List<MilkyIncomingSegmentModel>
) : GroupIncomingMessage {
    override val segments: List<Segment> =
        milkyIncomingData.map { convertSegment(this, it.data) }

    companion object {
        suspend fun fromGroupMessage(
            ctx: MilkyContext,
            data: MilkyGroupMessageData
        ) = ctx.getGroup(data.peerId)?.let { group ->
            ctx.getGroupMember(data.senderId, data.peerId)?.let { member ->
                MilkyIncomingGroupMessage(
                    ctx,
                    group,
                    member,
                    data.messageSeq,
                    Instant.fromEpochMilliseconds(data.time),
                    data.segments
                )
            }
        }
    }
}