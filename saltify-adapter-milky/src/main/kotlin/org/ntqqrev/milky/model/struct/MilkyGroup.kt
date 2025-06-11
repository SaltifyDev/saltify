package org.ntqqrev.milky.model.struct

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MilkyGroup(
    @SerialName("group_id")
    val groupId: Long,

    @SerialName("name")
    val name: String,

    @SerialName("member_count")
    val memberCount: Int,

    @SerialName("max_member_count")
    val maxMemberCount: Int,
)