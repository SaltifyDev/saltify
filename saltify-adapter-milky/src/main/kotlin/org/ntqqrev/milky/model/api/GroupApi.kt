package org.ntqqrev.milky.model.api

import org.ntqqrev.milky.model.struct.MilkyGroupAnnouncementData

internal class MilkySetGroupNameRequest(
    val groupId: Long,
    val name: String,
)

internal class MilkySetGroupAvatarRequest(
    val groupId: Long,
    val imageUri: String,
)

internal class MilkySetGroupMemberCardRequest(
    val groupId: Long,
    val userId: Long,
    val card: String,
)

internal class MilkySetGroupMemberSpecialTitleRequest(
    val groupId: Long,
    val userId: Long,
    val specialTitle: String,
)

internal class MilkySetGroupMemberAdminRequest(
    val groupId: Long,
    val userId: Long,
    val isSet: Boolean,
)

internal class MilkySetGroupMemberMuteRequest(
    val groupId: Long,
    val userId: Long,
    val duration: Long,
)

internal class MilkySetGroupWholeMuteRequest(
    val groupId: Long,
    val isMute: Boolean,
)

internal class MilkyKickGroupMemberRequest(
    val groupId: Long,
    val userId: Long,
    val rejectAddRequest: Boolean,
)

internal class MilkyGetGroupAnnouncementListRequest(
    val groupId: Long,
)

internal class MilkyGetGroupAnnouncementListResponse(
    val announcements: List<MilkyGroupAnnouncementData>,
)

internal class MilkySendGroupAnnouncementRequest(
    val groupId: Long,
    val content: String,
    val imageUri: String? = null,
)

internal class MilkyDeleteGroupAnnouncementRequest(
    val groupId: Long,
    val announcementId: Long,
)

internal class MilkyQuitGroupRequest(
    val groupId: Long,
)

internal class MilkySendGroupMessageReactionRequest(
    val groupId: Long,
    val messageSeq: Long,
    val reaction: String,
    val isAdd: Boolean = true,
)

internal class MilkySendGroupNudgeRequest(
    val groupId: Long,
    val userId: Long,
)