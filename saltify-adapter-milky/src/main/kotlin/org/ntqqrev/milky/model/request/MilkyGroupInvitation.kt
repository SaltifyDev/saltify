package org.ntqqrev.milky.model.request

internal data class MilkyGroupInvitation(
    val requestId: String,
    val time: Long,
    val isFiltered: Boolean,
    val initiatorId: Long,
    val state: String,
    val groupId: Long,
)