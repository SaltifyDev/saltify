package org.ntqqrev.milky.entity

import org.ntqqrev.milky.MilkyContext
import org.ntqqrev.milky.protocol.api.MilkyGetGroupListRequest
import org.ntqqrev.milky.protocol.api.MilkyGetGroupListResponse
import org.ntqqrev.milky.protocol.entity.MilkyGroupData
import org.ntqqrev.saltify.model.Group
import org.ntqqrev.saltify.utils.cache.AbstractCacheService
import org.ntqqrev.saltify.utils.cache.CachedEntity

class MilkyGroup(
    override val ctx: MilkyContext,
    override var dataBinding: MilkyGroupData,
) : Group, CachedEntity<MilkyGroupData> {
    override val uin: Long
        get() = dataBinding.groupId
    override val name: String
        get() = dataBinding.name
    override val memberCount: Int
        get() = dataBinding.memberCount
    override val maxMemberCount: Int
        get() = dataBinding.maxMemberCount

    internal val groupMemberCache = MilkyGroupMember.Cache(this)

    internal class Cache(val ctx: MilkyContext) :
        AbstractCacheService<MilkyGroup, Long, MilkyGroupData>(ctx.env.scope) {
        override suspend fun fetchData(): Map<Long, MilkyGroupData> =
            ctx.callApi<MilkyGetGroupListRequest, MilkyGetGroupListResponse>(
                "get_group_list",
                MilkyGetGroupListRequest(noCache = false)
            ).groups.associateBy { it.groupId }

        override fun constructNewEntity(data: MilkyGroupData): MilkyGroup =
            MilkyGroup(ctx, data)
    }
}