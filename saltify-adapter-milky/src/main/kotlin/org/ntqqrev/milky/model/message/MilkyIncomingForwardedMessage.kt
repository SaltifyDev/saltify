package org.ntqqrev.milky.model.message

internal class MilkyIncomingForwardedMessage(
    val userId: Long,
    val name: String,
    val segments: List<MilkyIncomingSegment>,
)