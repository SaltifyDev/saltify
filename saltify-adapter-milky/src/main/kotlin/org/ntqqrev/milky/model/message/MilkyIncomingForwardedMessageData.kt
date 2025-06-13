package org.ntqqrev.milky.model.message

class MilkyIncomingForwardedMessageData(
    val userId: Long,
    val name: String,
    val segments: List<MilkyIncomingSegmentModel>,
)