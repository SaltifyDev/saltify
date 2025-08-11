package org.ntqqrev.lagrange.cache

import org.ntqqrev.lagrange.LagrangeContext
import org.ntqqrev.lagrange.internal.packet.oidb.OidbFetchGroupMembersResponse
import org.ntqqrev.lagrange.internal.service.group.FetchGroupMembers
import org.ntqqrev.lagrange.model.LagrangeGroup
import org.ntqqrev.lagrange.model.LagrangeGroupMember

internal class GroupMemberCacheService(
    private val group: LagrangeGroup,
    ctx: LagrangeContext,
) : AbstractCacheService<LagrangeGroupMember, Long, OidbFetchGroupMembersResponse.Entry>(ctx) {
    override suspend fun fetchData(): Map<Long, OidbFetchGroupMembersResponse.Entry> {
        var token: String? = null
        val result = mutableMapOf<Long, OidbFetchGroupMembersResponse.Entry>()
        do {
            val fetchResult = ctx.client.callService(
                FetchGroupMembers,
                FetchGroupMembers.Req(group.uin, token)
            )
            token = fetchResult.token
            fetchResult.entries.forEach { result[it.identity.uin] = it }
        } while (token != null)
        return result
    }

    override fun constructNewEntity(
        existing: LagrangeGroupMember?,
        data: OidbFetchGroupMembersResponse.Entry
    ): LagrangeGroupMember = existing?.apply { this.data = data } ?: LagrangeGroupMember(group, data, ctx)
}


