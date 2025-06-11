package org.ntqqrev.milky.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MilkyGroupInvitation(
    @SerialName("request_id")
    val requestId: String,

    @SerialName("time")
    val time: Long,

    @SerialName("is_filtered")
    val isFiltered: Boolean,

    @SerialName("initiator_id")
    val initiatorId: Long,

    @SerialName("state")
    val state: String,

    @SerialName("group_id")
    val groupId: Long,
)