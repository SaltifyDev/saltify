package org.ntqqrev.milky.protocol.request

import org.ntqqrev.milky.protocol.event.MilkyEventBody

class MilkyGroupInvitationData(
    val requestId: String,
    val time: Long,
    val isFiltered: Boolean,
    val initiatorId: Long,
    val state: String,
    val groupId: Long,
) : MilkyEventBody