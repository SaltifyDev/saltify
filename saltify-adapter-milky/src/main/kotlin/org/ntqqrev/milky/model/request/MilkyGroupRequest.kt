package org.ntqqrev.milky.model.request

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "request_type",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(
        value = MilkyGroupJoinRequest::class,
        name = "join"
    ),
    JsonSubTypes.Type(
        value = MilkyGroupInviteRequest::class,
        name = "invite"
    )
)
internal sealed class MilkyGroupRequest(
    val requestId: String,
    val time: Long,
    val isFiltered: Boolean,
    val initiatorId: Long,
    val state: String,
    val groupId: Long,
    val operatorId: Long? = null,
)

internal class MilkyGroupJoinRequest(
    requestId: String,
    time: Long,
    isFiltered: Boolean,
    initiatorId: Long,
    state: String,
    groupId: Long,
    operatorId: Long? = null,
    val comment: String? = null,
) : MilkyGroupRequest(requestId, time, isFiltered, initiatorId, state, groupId, operatorId)

internal class MilkyGroupInviteRequest(
    requestId: String,
    time: Long,
    isFiltered: Boolean,
    initiatorId: Long,
    state: String,
    groupId: Long,
    operatorId: Long? = null,
    val inviteeId: Long,
) : MilkyGroupRequest(requestId, time, isFiltered, initiatorId, state, groupId, operatorId)