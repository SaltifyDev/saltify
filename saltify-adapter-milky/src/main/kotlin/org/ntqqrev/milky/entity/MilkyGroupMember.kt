package org.ntqqrev.milky.entity

import kotlinx.datetime.Instant
import org.ntqqrev.milky.MilkyContext
import org.ntqqrev.milky.model.api.MilkyGetGroupMemberListRequest
import org.ntqqrev.milky.model.api.MilkyGetGroupMemberListResponse
import org.ntqqrev.milky.model.struct.MilkyGroupMemberData
import org.ntqqrev.milky.util.toSaltifyGender
import org.ntqqrev.milky.util.toSaltifyRole
import org.ntqqrev.saltify.model.Gender
import org.ntqqrev.saltify.model.GroupMember
import org.ntqqrev.saltify.utils.cache.AbstractCacheService
import org.ntqqrev.saltify.utils.cache.CachedEntity

class MilkyGroupMember(
    override val group: MilkyGroup,
    override var dataBinding: MilkyGroupMemberData
) : GroupMember, CachedEntity<MilkyGroupMemberData> {
    override val ctx: MilkyContext
        get() = group.ctx
    override val uin: Long
        get() = dataBinding.userId
    override val nickname: String
        get() = dataBinding.nickname
    override val gender: Gender
        get() = dataBinding.sex.toSaltifyGender()
    override val card: String?
        get() = dataBinding.card
    override val specialTitle: String?
        get() = dataBinding.title
    override val level: Int
        get() = dataBinding.level
    override val role: GroupMember.Role
        get() = dataBinding.role.toSaltifyRole()
    override val joinedAt: Instant
        get() = Instant.fromEpochSeconds(dataBinding.joinTime)
    override val lastSpokeAt: Instant?
        get() = when (dataBinding.lastSentTime) {
            0L -> null
            else -> Instant.fromEpochSeconds(dataBinding.lastSentTime)
        }

    internal class Cache(val group: MilkyGroup) :
        AbstractCacheService<MilkyGroupMember, Long, MilkyGroupMemberData>(group.ctx.env.scope) {
        private val ctx get() = group.ctx

        override suspend fun fetchData(): Map<Long, MilkyGroupMemberData> =
            ctx.callApi<MilkyGetGroupMemberListRequest, MilkyGetGroupMemberListResponse>(
                "get_group_member_list",
                MilkyGetGroupMemberListRequest(groupId = group.uin, noCache = false)
            ).members.associateBy { it.userId }

        override fun constructNewEntity(data: MilkyGroupMemberData) = MilkyGroupMember(group, data)
    }
}