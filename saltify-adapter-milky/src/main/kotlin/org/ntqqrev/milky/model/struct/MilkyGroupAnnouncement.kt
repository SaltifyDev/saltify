package org.ntqqrev.milky.model.struct

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MilkyGroupAnnouncement(
    @SerialName("group_id")
    val groupId: Long,

    @SerialName("announcement_id")
    val announcementId: String,

    @SerialName("user_id")
    val userId: Long,

    @SerialName("time")
    val time: Long,

    @SerialName("content")
    val content: String,

    @SerialName("image_url")
    val imageUrl: String? = null,
)