package org.ntqqrev.saltify.model.group

import kotlinx.datetime.Instant
import org.ntqqrev.saltify.model.Group

interface Announcement {
    /**
     * The group the announcement belongs to.
     */
    val group: Group

    /**
     * The ID of the announcement.
     */
    val announcementId: String

    /**
     * The uin of the sender.
     * Not using `GroupMember` object because the sender may be no longer a member of the group.
     */
    val senderUin: Long

    /**
     * The time when the announcement was published.
     */
    val publishTime: Instant

    /**
     * The content of the announcement.
     */
    val content: String

    /**
     * The image URL of the announcement, if the announcement contains an image.
     *
     * For example:
     * `https://gdynamic.qpic.cn/gdynamic/TQYoNAmgslRDVQuzFvzPF728Vyicfs3YY1uOricUrU3RE/628`
     */
    val imageUrl: String?
}