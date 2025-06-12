package org.ntqqrev.milky.model.message

internal class MilkyOutgoingForwardedMessage(
    val userId: Long,
    val name: String,
    val segments: List<MilkyOutgoingSegment>,
) : MilkyOutgoingData()