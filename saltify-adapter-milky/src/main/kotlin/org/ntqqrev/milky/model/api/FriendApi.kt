package org.ntqqrev.milky.model.api

internal class MilkySendFriendNudgeRequest(
    val userId: Long,
    val isSelf: Boolean,
)

internal class MilkySendProfileLikeRequest(
    val userId: Long,
    val count: Int,
)