package org.ntqqrev.milky.protocol.api

import org.ntqqrev.milky.protocol.entity.MilkyGroupAnnouncementData

class MilkySetGroupNameRequest(
    val groupId: Long,
    val name: String,
)

class MilkySetGroupAvatarRequest(
    val groupId: Long,
    val imageUri: String,
)

class MilkySetGroupMemberCardRequest(
    val groupId: Long,
    val userId: Long,
    val card: String,
)

class MilkySetGroupMemberSpecialTitleRequest(
    val groupId: Long,
    val userId: Long,
    val specialTitle: String,
)

class MilkySetGroupMemberAdminRequest(
    val groupId: Long,
    val userId: Long,
    val isSet: Boolean,
)

class MilkySetGroupMemberMuteRequest(
    val groupId: Long,
    val userId: Long,
    val duration: Long,
)

class MilkySetGroupWholeMuteRequest(
    val groupId: Long,
    val isMute: Boolean,
)

class MilkyKickGroupMemberRequest(
    val groupId: Long,
    val userId: Long,
    val rejectAddRequest: Boolean,
)

class MilkyGetGroupAnnouncementListRequest(
    val groupId: Long,
)

class MilkyGetGroupAnnouncementListResponse(
    val announcements: List<MilkyGroupAnnouncementData>,
)

class MilkySendGroupAnnouncementRequest(
    val groupId: Long,
    val content: String,
    val imageUri: String? = null,
)

class MilkyDeleteGroupAnnouncementRequest(
    val groupId: Long,
    val announcementId: String,
)

class MilkyQuitGroupRequest(
    val groupId: Long,
)

class MilkySendGroupMessageReactionRequest(
    val groupId: Long,
    val messageSeq: Long,
    val reaction: String,
    val isAdd: Boolean = true,
)

class MilkySendGroupNudgeRequest(
    val groupId: Long,
    val userId: Long,
)