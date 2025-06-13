package org.ntqqrev.milky.model.api

import org.ntqqrev.milky.model.message.MilkyIncomingForwardedMessageData
import org.ntqqrev.milky.model.message.MilkyIncomingMessageData
import org.ntqqrev.milky.model.message.MilkyOutgoingSegmentModel

class MilkySendPrivateMessageRequest(
    val userId: Long,
    val message: List<MilkyOutgoingSegmentModel>,
)

class MilkySendPrivateMessageResponse(
    val messageSeq: Long,
    val time: Long,
)

class MilkySendGroupMessageRequest(
    val groupId: Long,
    val message: List<MilkyOutgoingSegmentModel>,
)

class MilkySendGroupMessageResponse(
    val messageSeq: Long,
    val time: Long,
)

class MilkyGetMessageRequest(
    val messageScene: String,
    val peerId: Long,
    val messageSeq: Long,
)

class MilkyGetMessageResponse(
    val message: MilkyIncomingMessageData,
)

class MilkyGetHistoryMessagesRequest(
    val messageScene: String,
    val peerId: Long,
    val startMessageSeq: Long?,
    val direction: String,
    val limit: Int = 20,
)

class MilkyGetHistoryMessagesResponse(
    val messages: List<MilkyIncomingMessageData>,
)

class MilkyGetResourceTempUrlRequest(
    val resourceId: String,
)

class MilkyGetResourceTempUrlResponse(
    val url: String,
)

class MilkyGetForwardedMessagesRequest(
    val forwardId: String,
)

class MilkyGetForwardedMessagesResponse(
    val messages: List<MilkyIncomingForwardedMessageData>,
)

class MilkyRecallPrivateMessageRequest(
    val userId: Long,
    val messageSeq: Long,
)

class MilkyRecallGroupMessageRequest(
    val groupId: Long,
    val messageSeq: Long,
)