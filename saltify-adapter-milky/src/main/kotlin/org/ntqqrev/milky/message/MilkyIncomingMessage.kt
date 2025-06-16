package org.ntqqrev.milky.message

import kotlinx.datetime.Instant
import org.ntqqrev.milky.MilkyContext
import org.ntqqrev.milky.protocol.message.*
import org.ntqqrev.milky.util.toImageSubType
import org.ntqqrev.saltify.Context
import org.ntqqrev.saltify.message.ImageSubType
import org.ntqqrev.saltify.message.incoming.*
import org.ntqqrev.saltify.model.Group
import org.ntqqrev.saltify.model.GroupMember
import org.ntqqrev.saltify.model.User

internal fun convertSegment(ctx: Context, data: MilkyIncomingData) =
    when (data) {
        is MilkyIncomingTextData -> TextSegment(ctx, data.text)
        is MilkyIncomingMentionData -> MentionSegment(ctx, data.userId)
        is MilkyIncomingMentionAllData -> MentionSegment(ctx, null)
        is MilkyIncomingFaceData -> FaceSegment(ctx, data.faceId)
        is MilkyIncomingReplyData -> ReplySegment(ctx, data.messageSeq)
        is MilkyIncomingImageData -> ImageSegment(
            ctx,
            data.resourceId,
            data.subType?.toImageSubType() ?: ImageSubType.NORMAL,
            data.summary ?: "",
        )

        is MilkyIncomingRecordData -> RecordSegment(
            ctx,
            data.resourceId,
            data.duration,
        )

        is MilkyIncomingVideoData -> VideoSegment(ctx, data.resourceId)
        is MilkyIncomingForwardData -> ForwardSegment(ctx, data.forwardId)
        is MilkyIncomingMarketFaceData -> MarketFaceSegment(ctx, data.url)
        is MilkyIncomingLightAppData -> LightAppSegment(
            ctx,
            data.appName,
            data.jsonPayload
        )

        is MilkyIncomingXmlData -> XmlSegment(
            ctx,
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
        milkyIncomingData.map { convertSegment(ctx, it.data) }

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
        milkyIncomingData.map { convertSegment(ctx, it.data) }

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

class MilkyIncomingForwardedMessage(
    override val ctx: Context,
    override val senderName: String,
    override val senderAvatarLink: String,
    override val time: Instant,
    milkyIncomingData: List<MilkyIncomingSegmentModel>,
) : ForwardedIncomingMessage {
    override val segments: List<Segment> =
        milkyIncomingData.map { convertSegment(ctx, it.data) }

    companion object {
        fun fromData(
            ctx: Context,
            data: MilkyIncomingForwardedMessageData,
        ) = MilkyIncomingForwardedMessage(
            ctx,
            data.name,
            data.avatarUrl,
            Instant.fromEpochMilliseconds(data.time),
            data.segments
        )
    }
}