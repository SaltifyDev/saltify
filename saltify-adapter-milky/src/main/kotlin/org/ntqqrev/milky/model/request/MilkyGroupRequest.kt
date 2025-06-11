package org.ntqqrev.milky.model.request

internal class MilkyGroupRequest(
    val requestId: String,
    val time: Long,
    val isFiltered: Boolean,
    val initiatorId: Long,
    val state: String,
    val groupId: Long,
    val operatorId: Long? = null,
    val requestType: String,
    val comment: String? = null,
    val inviteeId: Long? = null,
)