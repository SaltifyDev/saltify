package org.ntqqrev.milky.model.struct

internal data class MilkyGroup(
    val groupId: Long,
    val name: String,
    val memberCount: Int,
    val maxMemberCount: Int,
)