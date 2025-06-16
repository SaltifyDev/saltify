package org.ntqqrev.milky.protocol.message

class MilkyOutgoingForwardedMessageData(
    val userId: Long,
    val name: String,
    val segments: List<MilkyOutgoingSegmentModel>,
) : MilkyOutgoingData()