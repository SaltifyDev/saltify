package org.ntqqrev.milky.util

import org.ntqqrev.milky.protocol.message.MilkyIncomingData
import org.ntqqrev.milky.protocol.message.MilkyIncomingFaceData
import org.ntqqrev.milky.protocol.message.MilkyIncomingForwardData
import org.ntqqrev.milky.protocol.message.MilkyIncomingImageData
import org.ntqqrev.milky.protocol.message.MilkyIncomingLightAppData
import org.ntqqrev.milky.protocol.message.MilkyIncomingMarketFaceData
import org.ntqqrev.milky.protocol.message.MilkyIncomingMentionAllData
import org.ntqqrev.milky.protocol.message.MilkyIncomingMentionData
import org.ntqqrev.milky.protocol.message.MilkyIncomingMessageData
import org.ntqqrev.milky.protocol.message.MilkyIncomingRecordData
import org.ntqqrev.milky.protocol.message.MilkyIncomingReplyData
import org.ntqqrev.milky.protocol.message.MilkyIncomingTextData
import org.ntqqrev.milky.protocol.message.MilkyIncomingVideoData
import org.ntqqrev.milky.protocol.message.MilkyIncomingXmlData
import org.ntqqrev.saltify.Context
import org.ntqqrev.saltify.message.ImageSubType
import org.ntqqrev.saltify.message.incoming.FaceSegment
import org.ntqqrev.saltify.message.incoming.ForwardSegment
import org.ntqqrev.saltify.message.incoming.ImageSegment
import org.ntqqrev.saltify.message.incoming.LightAppSegment
import org.ntqqrev.saltify.message.incoming.MarketFaceSegment
import org.ntqqrev.saltify.message.incoming.MentionSegment
import org.ntqqrev.saltify.message.incoming.RecordSegment
import org.ntqqrev.saltify.message.incoming.ReplySegment
import org.ntqqrev.saltify.message.incoming.TextSegment
import org.ntqqrev.saltify.message.incoming.VideoSegment
import org.ntqqrev.saltify.message.incoming.XmlSegment

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