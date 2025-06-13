package org.ntqqrev.milky.model.message

class MilkyOutgoingForwardedMessageData(
    val userId: Long,
    val name: String,
    val segments: List<MilkyOutgoingSegmentModel>,
) : MilkyOutgoingData()