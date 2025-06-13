package org.ntqqrev.milky.model.message

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.ntqqrev.milky.model.event.MilkyEventBody
import org.ntqqrev.milky.model.struct.MilkyFriendData
import org.ntqqrev.milky.model.struct.MilkyGroupData
import org.ntqqrev.milky.model.struct.MilkyGroupMemberData

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "message_scene",
)
@JsonSubTypes(
    JsonSubTypes.Type(MilkyFriendMessageData::class, "friend"),
    JsonSubTypes.Type(MilkyGroupMessageData::class, "group"),
    JsonSubTypes.Type(MilkyTempMessageData::class, "temp"),
)
internal sealed class MilkyIncomingMessageData(
    val peerId: Long,
    val messageSeq: Long,
    val senderId: Long,
    val time: Long,
    val segments: List<MilkyIncomingSegmentModel>,
) : MilkyEventBody

class MilkyFriendMessageData(
    peerId: Long,
    messageSeq: Long,
    senderId: Long,
    time: Long,
    segments: List<MilkyIncomingSegmentModel>,
    val friend: MilkyFriendData,
) : MilkyIncomingMessageData(peerId, messageSeq, senderId, time, segments)

class MilkyGroupMessageData(
    peerId: Long,
    messageSeq: Long,
    senderId: Long,
    time: Long,
    segments: List<MilkyIncomingSegmentModel>,
    val group: MilkyGroupData,
    val groupMember: MilkyGroupMemberData,
) : MilkyIncomingMessageData(peerId, messageSeq, senderId, time, segments)

class MilkyTempMessageData(
    peerId: Long,
    messageSeq: Long,
    senderId: Long,
    time: Long,
    segments: List<MilkyIncomingSegmentModel>,
    val group: MilkyGroupData? = null, // 可选
) : MilkyIncomingMessageData(peerId, messageSeq, senderId, time, segments)