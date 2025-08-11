package org.ntqqrev.milky.entity

import kotlinx.datetime.Instant
import org.ntqqrev.milky.protocol.entity.MilkyGroupAnnouncementData
import org.ntqqrev.saltify.model.Group
import org.ntqqrev.saltify.model.group.Announcement

class MilkyAnnouncement(
    override val group: MilkyGroup,
    data: MilkyGroupAnnouncementData
) : Announcement {
    override val announcementId: String = data.announcementId
    override val senderUin: Long = data.userId
    override val publishTime: Instant = Instant.fromEpochSeconds(data.time)
    override val content: String = data.content
    override val imageUrl: String? = data.imageUrl
}