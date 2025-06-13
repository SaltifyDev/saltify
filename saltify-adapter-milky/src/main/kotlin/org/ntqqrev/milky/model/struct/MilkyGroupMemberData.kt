package org.ntqqrev.milky.model.struct

class MilkyGroupMemberData(
    val groupId: Long,
    val userId: Long,
    val nickname: String,
    val card: String? = null,
    val title: String? = null,
    val sex: String,
    val level: Int,
    val role: String,
    val joinTime: Long,
    val lastSentTime: Long,
)