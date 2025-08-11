package org.ntqqrev.lagrange.cache

import org.ntqqrev.lagrange.LagrangeContext
import org.ntqqrev.lagrange.internal.service.friend.FetchFriends
import org.ntqqrev.lagrange.model.LagrangeFriend

internal class FriendCacheService(ctx: LagrangeContext) :
    AbstractCacheService<LagrangeFriend, Long, FetchFriends.Entry>(ctx) {
    override suspend fun fetchData(): Map<Long, FetchFriends.Entry> {
        var nextUin: Long? = null
        val result = mutableMapOf<Long, FetchFriends.Entry>()
        do {
            val fetchResult = ctx.client.callService(FetchFriends, FetchFriends.Req(nextUin))
            nextUin = fetchResult.nextUin
            fetchResult.entries.forEach { result[it.uin] = it }
        } while (nextUin != null)
        return result
    }

    override fun constructNewEntity(existing: LagrangeFriend?, data: FetchFriends.Entry): LagrangeFriend =
        existing?.apply { this.data = data } ?: LagrangeFriend(data, ctx)
}


