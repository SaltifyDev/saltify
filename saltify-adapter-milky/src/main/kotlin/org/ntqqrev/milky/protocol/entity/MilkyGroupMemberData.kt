package org.ntqqrev.milky.protocol.entity

class MilkyGroupMemberData(
    val userId: Long,
    val nickname: String,
    val sex: String,
    val card: String? = null,
    val groupId: Long,
    val title: String? = null,
    val level: Int,
    val role: String,
    val joinTime: Long,
    val lastSentTime: Long? = null,
    val shutUpEndTime: Long? = null,
)