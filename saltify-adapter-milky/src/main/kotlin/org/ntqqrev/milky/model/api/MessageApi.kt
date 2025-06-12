package org.ntqqrev.milky.model.api

import org.ntqqrev.milky.model.message.MilkyIncomingForwardedMessage
import org.ntqqrev.milky.model.message.MilkyIncomingMessage
import org.ntqqrev.milky.model.message.MilkyOutgoingSegment

internal class MilkySendPrivateMessageRequest(
    val userId: Long,
    val message: List<MilkyOutgoingSegment>,
)

internal class MilkySendPrivateMessageResponse(
    val messageSeq: Long,
    val time: Long,
)

internal class MilkySendGroupMessageRequest(
    val groupId: Long,
    val message: List<MilkyOutgoingSegment>,
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
    val message: MilkyIncomingMessage,
)

internal class MilkyGetHistoryMessagesRequest(
    val messageScene: String,
    val peerId: Long,
    val startMessageSeq: Long?,
    val direction: String,
    val limit: Int = 20,
)

internal class MilkyGetHistoryMessagesResponse(
    val messages: List<MilkyIncomingMessage>,
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
    val messages: List<MilkyIncomingForwardedMessage>,
)

internal class MilkyRecallPrivateMessageRequest(
    val userId: Long,
    val messageSeq: Long,
)

internal class MilkyRecallGroupMessageRequest(
    val groupId: Long,
    val messageSeq: Long,
)