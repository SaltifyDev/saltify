package org.ntqqrev.milky.protocol.message

import org.ntqqrev.milky.protocol.event.MilkyEventBody
import org.ntqqrev.milky.protocol.entity.MilkyFriendData
import org.ntqqrev.milky.protocol.entity.MilkyGroupData
import org.ntqqrev.milky.protocol.entity.MilkyGroupMemberData

class MilkyIncomingMessageData(
    val messageScene: String,
    val peerId: Long,
    val messageSeq: Long,
    val senderId: Long,
    val time: Long,
    val segments: List<MilkyIncomingSegmentModel>,
    val friend: MilkyFriendData? = null,
    val group: MilkyGroupData? = null,
    val groupMember: MilkyGroupMemberData? = null,
) : MilkyEventBody