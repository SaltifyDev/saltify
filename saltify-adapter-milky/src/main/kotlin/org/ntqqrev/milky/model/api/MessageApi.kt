package org.ntqqrev.milky.model.api

import org.ntqqrev.milky.model.message.MilkyIncomingForwardedMessageData
import org.ntqqrev.milky.model.message.MilkyIncomingMessageData
import org.ntqqrev.milky.model.message.MilkyOutgoingSegmentModel

internal class MilkySendPrivateMessageRequest(
    val userId: Long,
    val message: List<MilkyOutgoingSegmentModel>,
)

internal class MilkySendPrivateMessageResponse(
    val messageSeq: Long,
    val time: Long,
)

internal class MilkySendGroupMessageRequest(
    val groupId: Long,
    val message: List<MilkyOutgoingSegmentModel>,
)

internal class MilkySendGroupMessageResponse(
    val messageSeq: Long,
    val time: Long,
)

internal class MilkyGetMessageRequest(
    val messageScene: String,
    val peerId: Long,
    val messageSeq: Long,
)

internal class MilkyGetMessageResponse(
    val message: MilkyIncomingMessageData,
)

internal class MilkyGetHistoryMessagesRequest(
    val messageScene: String,
    val peerId: Long,
    val startMessageSeq: Long?,
    val direction: String,
    val limit: Int = 20,
)

internal class MilkyGetHistoryMessagesResponse(
    val messages: List<MilkyIncomingMessageData>,
)

internal class MilkyGetResourceTempUrlRequest(
    val resourceId: String,
)

internal class MilkyGetResourceTempUrlResponse(
    val url: String,
)

internal class MilkyGetForwardedMessagesRequest(
    val forwardId: String,
)

internal class MilkyGetForwardedMessagesResponse(
    val messages: List<MilkyIncomingForwardedMessageData>,
)

internal class MilkyRecallPrivateMessageRequest(
    val userId: Long,
    val messageSeq: Long,
)

internal class MilkyRecallGroupMessageRequest(
    val groupId: Long,
    val messageSeq: Long,
)