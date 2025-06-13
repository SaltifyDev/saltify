package org.ntqqrev.milky.model.struct

internal class MilkyFriendData(
    val userId: Long,
    val qid: String? = null,
    val nickname: String,
    val sex: String,
    val remark: String,
    val category: MilkyFriendCategoryData,
)