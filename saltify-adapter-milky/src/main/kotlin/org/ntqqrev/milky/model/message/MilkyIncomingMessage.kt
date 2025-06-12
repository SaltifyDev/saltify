package org.ntqqrev.milky.model.message

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.ntqqrev.milky.model.struct.MilkyFriend
import org.ntqqrev.milky.model.struct.MilkyGroup
import org.ntqqrev.milky.model.struct.MilkyGroupMember

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "message_scene",
)
@JsonSubTypes(
    JsonSubTypes.Type(MilkyFriendMessage::class, "friend"),
    JsonSubTypes.Type(MilkyGroupMessage::class, "group"),
    JsonSubTypes.Type(MilkyTempMessage::class, "temp"),
)
internal sealed class MilkyIncomingMessage(
    val peerId: Long,
    val messageSeq: Long,
    val senderId: Long,
    val time: Long,
    val segments: List<MilkyIncomingSegment>,
)

internal class MilkyFriendMessage(
    peerId: Long,
    messageSeq: Long,
    senderId: Long,
    time: Long,
    segments: List<MilkyIncomingSegment>,
    val friend: MilkyFriend,
) : MilkyIncomingMessage(peerId, messageSeq, senderId, time, segments)

internal class MilkyGroupMessage(
    peerId: Long,
    messageSeq: Long,
    senderId: Long,
    time: Long,
    segments: List<MilkyIncomingSegment>,
    val group: MilkyGroup,
    val groupMember: MilkyGroupMember,
) : MilkyIncomingMessage(peerId, messageSeq, senderId, time, segments)

internal class MilkyTempMessage(
    peerId: Long,
    messageSeq: Long,
    senderId: Long,
    time: Long,
    segments: List<MilkyIncomingSegment>,
    val group: MilkyGroup? = null, // 可选
) : MilkyIncomingMessage(peerId, messageSeq, senderId, time, segments)