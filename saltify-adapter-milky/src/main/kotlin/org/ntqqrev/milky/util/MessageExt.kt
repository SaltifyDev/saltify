package org.ntqqrev.milky.util

import org.ntqqrev.milky.MilkyContext
import org.ntqqrev.milky.protocol.message.*
import org.ntqqrev.saltify.message.ImageSubType
import org.ntqqrev.saltify.message.incoming.*

internal fun MilkyIncomingMessageData.resolveSenderName(): String {
    if (friend != null) {
        return friend.remark.ifEmpty {
            friend.nickname
        }
    } else if (groupMember != null) {
        return groupMember.card.ifEmpty {
            groupMember.nickname
        }
    } else {
        return ""
    }
}

internal fun convertSegment(ctx: MilkyContext, data: MilkyIncomingData) =
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