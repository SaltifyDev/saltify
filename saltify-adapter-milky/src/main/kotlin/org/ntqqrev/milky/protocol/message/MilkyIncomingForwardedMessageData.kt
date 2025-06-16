package org.ntqqrev.milky.protocol.message

class MilkyIncomingForwardedMessageData(
    val name: String,
    val avatarUrl: String,
    val time: Long,
    val segments: List<MilkyIncomingSegmentModel>,
)