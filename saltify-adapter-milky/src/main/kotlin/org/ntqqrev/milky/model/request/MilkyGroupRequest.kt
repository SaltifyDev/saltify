package org.ntqqrev.milky.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MilkyGroupRequest(
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

    @SerialName("operator_id")
    val operatorId: Long? = null,

    @SerialName("request_type")
    val requestType: String,

    @SerialName("comment")
    val comment: String? = null,

    @SerialName("invitee_id")
    val inviteeId: Long? = null,
)