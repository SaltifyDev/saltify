package org.ntqqrev.lagrange.cache

import org.ntqqrev.lagrange.LagrangeContext
import org.ntqqrev.lagrange.internal.service.group.FetchGroups
import org.ntqqrev.lagrange.model.LagrangeGroup
import org.ntqqrev.lagrange.internal.packet.oidb.OidbFetchGroupsResponse

internal class GroupCacheService(ctx: LagrangeContext) :
    AbstractCacheService<LagrangeGroup, Long, OidbFetchGroupsResponse.Entry>(ctx) {
    override suspend fun fetchData(): Map<Long, OidbFetchGroupsResponse.Entry> =
        ctx.client.callService(FetchGroups).entries.associateBy { it.groupUin }

    override fun constructNewEntity(existing: LagrangeGroup?, data: OidbFetchGroupsResponse.Entry): LagrangeGroup =
        existing?.apply { this.data = data } ?: LagrangeGroup(data, ctx)
}


