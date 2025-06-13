package org.ntqqrev.milky.model.api

class MilkySendFriendNudgeRequest(
    val userId: Long,
    val isSelf: Boolean,
)

class MilkySendProfileLikeRequest(
    val userId: Long,
    val count: Int,
)