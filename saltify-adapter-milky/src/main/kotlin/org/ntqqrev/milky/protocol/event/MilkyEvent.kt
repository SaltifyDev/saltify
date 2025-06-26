package org.ntqqrev.milky.protocol.event

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.ntqqrev.milky.protocol.message.MilkyIncomingMessageData
import org.ntqqrev.milky.protocol.request.MilkyFriendRequestData
import org.ntqqrev.milky.protocol.request.MilkyGroupInvitationData
import org.ntqqrev.milky.protocol.request.MilkyGroupRequestData

class MilkyEvent(
    val time: Long,
    val selfId: Long,
    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "event_type",
    )
    @JsonSubTypes(
        JsonSubTypes.Type(MilkyBotOfflineEvent::class, "bot_offline"),
        JsonSubTypes.Type(MilkyIncomingMessageData::class, "message_receive"),
        JsonSubTypes.Type(MilkyMessageRecallEvent::class, "message_recall"),
        JsonSubTypes.Type(MilkyFriendRequestData::class, "friend_request"),
        JsonSubTypes.Type(MilkyGroupRequestData::class, "group_request"),
        JsonSubTypes.Type(MilkyGroupInvitationData::class, "group_invitation"),
        JsonSubTypes.Type(MilkyFriendNudgeEvent::class, "friend_nudge"),
        JsonSubTypes.Type(MilkyFriendFileUploadEvent::class, "friend_file_upload"),
        JsonSubTypes.Type(MilkyGroupAdminChangeEvent::class, "group_admin_change"),
        JsonSubTypes.Type(MilkyGroupEssenceMessageChangeEvent::class, "group_essence_message_change"),
        JsonSubTypes.Type(MilkyGroupMemberIncreaseEvent::class, "group_member_increase"),
        JsonSubTypes.Type(MilkyGroupMemberDecreaseEvent::class, "group_member_decrease"),
        JsonSubTypes.Type(MilkyGroupNameChangeEvent::class, "group_name_change"),
        JsonSubTypes.Type(MilkyGroupMessageReactionEvent::class, "group_message_reaction"),
        JsonSubTypes.Type(MilkyGroupMuteEvent::class, "group_mute"),
        JsonSubTypes.Type(MilkyGroupWholeMuteEvent::class, "group_whole_mute"),
        JsonSubTypes.Type(MilkyGroupNudgeEvent::class, "group_nudge"),
        JsonSubTypes.Type(MilkyGroupFileUploadEvent::class, "group_file_upload")
    )
    val data: MilkyEventBody
)

interface MilkyEventBody

class MilkyBotOfflineEvent(
    val reason: String,
) : MilkyEventBody

class MilkyMessageRecallEvent(
    val messageScene: String,
    val peerId: Long,
    val messageSeq: Long,
    val senderId: Long,
    val operatorId: Long? = null,
) : MilkyEventBody

class MilkyFriendNudgeEvent(
    val userId: Long,
    val isSelfSend: Boolean,
    val isSelfReceive: Boolean,
) : MilkyEventBody

class MilkyFriendFileUploadEvent(
    val userId: Long,
    val fileId: String,
    val fileName: String,
    val fileSize: Long,
    val isSelf: Boolean,
) : MilkyEventBody

class MilkyGroupAdminChangeEvent(
    val groupId: Long,
    val userId: Long,
    val isSet: Boolean,
) : MilkyEventBody

class MilkyGroupEssenceMessageChangeEvent(
    val groupId: Long,
    val messageSeq: Long,
    val isSet: Boolean,
) : MilkyEventBody

class MilkyGroupMemberIncreaseEvent(
    val groupId: Long,
    val userId: Long,
    val operatorId: Long? = null,
    val invitorId: Long? = null,
) : MilkyEventBody

class MilkyGroupMemberDecreaseEvent(
    val groupId: Long,
    val userId: Long,
    val operatorId: Long? = null,
) : MilkyEventBody

class MilkyGroupNameChangeEvent(
    val groupId: Long,
    val name: String,
    val operatorId: Long,
) : MilkyEventBody

class MilkyGroupMessageReactionEvent(
    val groupId: Long,
    val userId: Long,
    val messageSeq: Long,
    val faceId: String,
    val isAdd: Boolean = true,
) : MilkyEventBody

class MilkyGroupMuteEvent(
    val groupId: Long,
    val userId: Long,
    val operatorId: Long,
    val duration: Int, // 0 means unmute
) : MilkyEventBody

class MilkyGroupWholeMuteEvent(
    val groupId: Long,
    val operatorId: Long,
    val isMute: Boolean,
) : MilkyEventBody

class MilkyGroupNudgeEvent(
    val groupId: Long,
    val senderId: Long,
    val receiverId: Long,
) : MilkyEventBody

class MilkyGroupFileUploadEvent(
    val groupId: Long,
    val userId: Long,
    val fileId: String,
    val fileName: String,
    val fileSize: Long,
) : MilkyEventBody