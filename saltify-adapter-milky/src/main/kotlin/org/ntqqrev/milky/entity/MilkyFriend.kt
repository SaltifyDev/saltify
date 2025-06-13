package org.ntqqrev.milky.entity

import org.ntqqrev.milky.MilkyContext
import org.ntqqrev.milky.model.api.MilkyGetFriendListRequest
import org.ntqqrev.milky.model.api.MilkyGetFriendListResponse
import org.ntqqrev.milky.model.struct.MilkyFriendData
import org.ntqqrev.milky.util.toSaltifyGender
import org.ntqqrev.saltify.model.Friend
import org.ntqqrev.saltify.model.Gender
import org.ntqqrev.saltify.utils.cache.AbstractCacheService
import org.ntqqrev.saltify.utils.cache.CachedEntity

class MilkyFriend(
    override val ctx: MilkyContext,
    override var dataBinding: MilkyFriendData,
) : Friend, CachedEntity<MilkyFriendData> {
    override val uin: Long
        get() = dataBinding.userId
    override val qid: String?
        get() = dataBinding.qid
    override val nickname: String
        get() = dataBinding.nickname
    override val gender: Gender
        get() = dataBinding.sex.toSaltifyGender()
    override val remark: String?
        get() = dataBinding.remark
    override val category: Int
        get() = dataBinding.category.categoryId

    internal class Cache(val ctx: MilkyContext) :
        AbstractCacheService<MilkyFriend, Long, MilkyFriendData>(ctx.env.scope) {
        override suspend fun fetchData(): Map<Long, MilkyFriendData> =
            ctx.callApi<MilkyGetFriendListRequest, MilkyGetFriendListResponse>(
                "get_friend_list",
                MilkyGetFriendListRequest(noCache = false)
            ).friends.associateBy { it.userId }

        override fun constructNewEntity(data: MilkyFriendData): MilkyFriend =
            MilkyFriend(ctx, data)
    }
}