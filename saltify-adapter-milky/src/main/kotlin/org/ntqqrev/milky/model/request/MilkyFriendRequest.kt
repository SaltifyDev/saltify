package org.ntqqrev.milky.model.request

internal class MilkyFriendRequest(
    val requestId: String,
    val time: Long,
    val isFiltered: Boolean,
    val initiatorId: Long,
    val state: String,
    val comment: String? = null,
    val via: String? = null,
)