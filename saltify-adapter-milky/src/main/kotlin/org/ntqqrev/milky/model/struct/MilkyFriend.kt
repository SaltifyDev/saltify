package org.ntqqrev.milky.model.struct

internal class MilkyFriend(
    val userId: Long,
    val qid: String? = null,
    val nickname: String,
    val sex: String,
    val remark: String,
    val category: MilkyFriendCategory,
)