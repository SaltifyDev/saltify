package org.ntqqrev.milky.model.struct

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MilkyGroupFile(
    @SerialName("group_id")
    val groupId: Long,

    @SerialName("file_id")
    val fileId: String,

    @SerialName("file_name")
    val fileName: String,

    @SerialName("parent_folder_id")
    val parentFolderId: String? = null,

    @SerialName("file_size")
    val fileSize: Long,

    @SerialName("uploaded_time")
    val uploadedTime: Long,

    @SerialName("expire_time")
    val expireTime: Long,

    @SerialName("uploader_id")
    val uploaderId: Long,

    @SerialName("downloaded_times")
    val downloadedTimes: Int,
)