package org.ntqqrev.milky.model.request

import org.ntqqrev.milky.model.event.MilkyEventBody

class MilkyGroupInvitationData(
    val requestId: String,
    val time: Long,
    val isFiltered: Boolean,
    val initiatorId: Long,
    val state: String,
    val groupId: Long,
) : MilkyEventBody