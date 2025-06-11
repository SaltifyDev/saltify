package org.ntqqrev.milky.model.struct

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MilkyGroupMember(
    @SerialName("group_id")
    val groupId: Long,

    @SerialName("user_id")
    val userId: Long,

    @SerialName("nickname")
    val nickname: String,

    @SerialName("card")
    val card: String? = null,

    @SerialName("title")
    val title: String? = null,

    @SerialName("sex")
    val sex: String,

    @SerialName("level")
    val level: Int,

    @SerialName("role")
    val role: String,

    @SerialName("join_time")
    val joinTime: Long,

    @SerialName("last_sent_time")
    val lastSentTime: Long,
)