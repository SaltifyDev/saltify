package org.ntqqrev.milky.model.struct

internal class MilkyGroupAnnouncement(
    val groupId: Long,
    val announcementId: String,
    val userId: Long,
    val time: Long,
    val content: String,
    val imageUrl: String? = null,
)