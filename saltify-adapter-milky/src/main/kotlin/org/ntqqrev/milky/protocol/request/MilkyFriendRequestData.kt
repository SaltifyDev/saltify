package org.ntqqrev.milky.protocol.request

import org.ntqqrev.milky.protocol.event.MilkyEventBody

class MilkyFriendRequestData(
    val requestId: String,
    val time: Long,
    val isFiltered: Boolean,
    val initiatorId: Long,
    val state: String,
    val comment: String? = null,
    val via: String? = null,
) : MilkyEventBody