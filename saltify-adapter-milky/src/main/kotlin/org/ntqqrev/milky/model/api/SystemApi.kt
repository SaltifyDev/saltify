package org.ntqqrev.milky.model.api

import org.ntqqrev.milky.model.struct.MilkyFriendData
import org.ntqqrev.milky.model.struct.MilkyGroupData
import org.ntqqrev.milky.model.struct.MilkyGroupMemberData

class MilkyGetLoginInfoResponse(
    val uin: Long,
    val nickname: String,
)

class MilkyGetImplInfoResponse(
    val implName: String,
    val implVersion: String,
    val qqProtocolVersion: String,
    val qqProtocolType: String,
    val milkyVersion: String,
)

class MilkyGetFriendListRequest(
    val noCache: Boolean,
)

class MilkyGetFriendListResponse(
    val friends: List<MilkyFriendData>,
)

class MilkyGetFriendInfoRequest(
    val userId: Long,
    val noCache: Boolean,
)

class MilkyGetFriendInfoResponse(
    val friend: MilkyFriendData,
)

class MilkyGetGroupListRequest(
    val noCache: Boolean,
)

class MilkyGetGroupListResponse(
    val groups: List<MilkyGroupData>,
)

class MilkyGetGroupInfoRequest(
    val groupId: Long,
    val noCache: Boolean,
)

class MilkyGetGroupInfoResponse(
    val group: MilkyGroupData,
)

class MilkyGetGroupMemberListRequest(
    val groupId: Long,
    val noCache: Boolean,
)

class MilkyGetGroupMemberListResponse(
    val members: List<MilkyGroupMemberData>,
)

class MilkyGetGroupMemberInfoRequest(
    val groupId: Long,
    val userId: Long,
    val noCache: Boolean,
)

class MilkyGetGroupMemberInfoResponse(
    val member: MilkyGroupMemberData,
)