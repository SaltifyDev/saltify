package org.ntqqrev.milky.model.api

import org.ntqqrev.milky.model.struct.MilkyFriend
import org.ntqqrev.milky.model.struct.MilkyGroup
import org.ntqqrev.milky.model.struct.MilkyGroupMember

internal class MilkyGetLoginInfoResponse(
    val uin: Long,
    val nickname: String,
)

internal class MilkyGetImplInfoResponse(
    val implName: String,
    val implVersion: String,
    val qqProtocolVersion: String,
    val qqProtocolType: String,
    val milkyVersion: String,
)

internal class MilkyGetFriendListRequest(
    val noCache: Boolean,
)

internal class MilkyGetFriendListResponse(
    val friends: List<MilkyFriend>,
)

internal class MilkyGetFriendInfoRequest(
    val userId: Long,
    val noCache: Boolean,
)

internal class MilkyGetFriendInfoResponse(
    val friend: MilkyFriend,
)

internal class MilkyGetGroupListRequest(
    val noCache: Boolean,
)

internal class MilkyGetGroupListResponse(
    val groups: List<MilkyGroup>,
)

internal class MilkyGetGroupInfoRequest(
    val groupId: Long,
    val noCache: Boolean,
)

internal class MilkyGetGroupInfoResponse(
    val group: MilkyGroup,
)

internal class MilkyGetGroupMemberListRequest(
    val groupId: Long,
    val noCache: Boolean,
)

internal class MilkyGetGroupMemberListResponse(
    val members: List<MilkyGroupMember>,
)

internal class MilkyGetGroupMemberInfoRequest(
    val groupId: Long,
    val userId: Long,
    val noCache: Boolean,
)

internal class MilkyGetGroupMemberInfoResponse(
    val member: MilkyGroupMember,
)