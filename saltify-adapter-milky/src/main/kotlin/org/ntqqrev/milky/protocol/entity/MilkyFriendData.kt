package org.ntqqrev.milky.protocol.entity

class MilkyFriendData(
    val userId: Long,
    val qid: String? = null,
    val nickname: String,
    val sex: String,
    val remark: String,
    val category: MilkyFriendCategoryData,
)