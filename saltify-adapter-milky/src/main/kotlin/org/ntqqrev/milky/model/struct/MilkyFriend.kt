package org.ntqqrev.milky.model.struct

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MilkyFriend(
    @SerialName("user_id")
    val userId: Long,

    @SerialName("qid")
    val qid: String? = null,

    @SerialName("nickname")
    val nickname: String,

    @SerialName("sex")
    val sex: String,

    @SerialName("remark")
    val remark: String,

    @SerialName("category")
    val category: MilkyFriendCategory,
)