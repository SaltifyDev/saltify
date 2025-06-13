package org.ntqqrev.milky.model.request

import org.ntqqrev.milky.model.event.MilkyEventBody

class MilkyFriendRequestData(
    val requestId: String,
    val time: Long,
    val isFiltered: Boolean,
    val initiatorId: Long,
    val state: String,
    val comment: String? = null,
    val via: String? = null,
) : MilkyEventBody