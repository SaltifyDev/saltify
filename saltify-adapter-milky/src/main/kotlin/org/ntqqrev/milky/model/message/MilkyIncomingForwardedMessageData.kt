package org.ntqqrev.milky.model.message

class MilkyIncomingForwardedMessageData(
    val name: String,
    val avatarUrl: String,
    val time: Long,
    val segments: List<MilkyIncomingSegmentModel>,
)