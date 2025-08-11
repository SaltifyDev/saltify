package org.ntqqrev.milky.protocol.message

class MilkyOutgoingForwardedMessageData(
    val userId: Long,
    val senderName: String,
    val segments: List<MilkyOutgoingSegmentModel>,
) : MilkyOutgoingData()