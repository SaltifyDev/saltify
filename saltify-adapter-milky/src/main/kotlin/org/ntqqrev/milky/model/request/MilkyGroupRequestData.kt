package org.ntqqrev.milky.model.request

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.ntqqrev.milky.model.event.MilkyEventBody

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "request_type",
)
@JsonSubTypes(
    JsonSubTypes.Type(MilkyGroupJoinRequestData::class, "join"),
    JsonSubTypes.Type(MilkyGroupInviteRequestData::class, "invite")
)
internal sealed class MilkyGroupRequestData(
    val requestId: String,
    val time: Long,
    val isFiltered: Boolean,
    val initiatorId: Long,
    val state: String,
    val groupId: Long,
    val operatorId: Long? = null,
) : MilkyEventBody

class MilkyGroupJoinRequestData(
    requestId: String,
    time: Long,
    isFiltered: Boolean,
    initiatorId: Long,
    state: String,
    groupId: Long,
    operatorId: Long? = null,
    val comment: String? = null,
) : MilkyGroupRequestData(requestId, time, isFiltered, initiatorId, state, groupId, operatorId)

class MilkyGroupInviteRequestData(
    requestId: String,
    time: Long,
    isFiltered: Boolean,
    initiatorId: Long,
    state: String,
    groupId: Long,
    operatorId: Long? = null,
    val inviteeId: Long,
) : MilkyGroupRequestData(requestId, time, isFiltered, initiatorId, state, groupId, operatorId)