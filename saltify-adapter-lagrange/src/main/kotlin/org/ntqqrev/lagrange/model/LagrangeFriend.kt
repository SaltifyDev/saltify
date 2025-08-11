package org.ntqqrev.lagrange.model

import org.ntqqrev.lagrange.LagrangeContext
import org.ntqqrev.lagrange.internal.service.friend.FetchFriends
import org.ntqqrev.saltify.model.Friend

internal class LagrangeFriend(
    var data: FetchFriends.Entry,
    override val ctx: LagrangeContext
) : Friend {
    override val uin: Long get() = data.uin
    override val nickname: String get() = data.nickname ?: uin.toString()
    override val gender = org.ntqqrev.saltify.model.Gender.UNKNOWN
    val signature: String? get() = data.signature
    override val remark: String? get() = data.remark
    override val qid: String? get() = data.qid
    override val category: Int get() = data.category
    override fun toString(): String = "${remark ?: nickname} ($uin)"
}


