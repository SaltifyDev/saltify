package org.ntqqrev.milky.model.api

import org.ntqqrev.milky.model.struct.MilkyFriendData
import org.ntqqrev.milky.model.struct.MilkyGroupData
import org.ntqqrev.milky.model.struct.MilkyGroupMemberData

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
    val friends: List<MilkyFriendData>,
)

internal class MilkyGetFriendInfoRequest(
    val userId: Long,
    val noCache: Boolean,
)

internal class MilkyGetFriendInfoResponse(
    val friend: MilkyFriendData,
)

internal class MilkyGetGroupListRequest(
    val noCache: Boolean,
)

internal class MilkyGetGroupListResponse(
    val groups: List<MilkyGroupData>,
)

internal class MilkyGetGroupInfoRequest(
    val groupId: Long,
    val noCache: Boolean,
)

internal class MilkyGetGroupInfoResponse(
    val group: MilkyGroupData,
)

internal class MilkyGetGroupMemberListRequest(
    val groupId: Long,
    val noCache: Boolean,
)

internal class MilkyGetGroupMemberListResponse(
    val members: List<MilkyGroupMemberData>,
)

internal class MilkyGetGroupMemberInfoRequest(
    val groupId: Long,
    val userId: Long,
    val noCache: Boolean,
)

internal class MilkyGetGroupMemberInfoResponse(
    val member: MilkyGroupMemberData,
)