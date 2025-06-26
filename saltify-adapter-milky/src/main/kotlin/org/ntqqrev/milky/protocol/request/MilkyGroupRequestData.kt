package org.ntqqrev.milky.protocol.request

import org.ntqqrev.milky.protocol.event.MilkyEventBody

sealed class MilkyGroupRequestData(
    val requestType: String,
    val requestId: String,
    val time: Long,
    val isFiltered: Boolean,
    val initiatorId: Long,
    val state: String,
    val groupId: Long,
    val operatorId: Long? = null,
    val comment: String? = null,
    val inviteeId: Long? = null,
) : MilkyEventBody