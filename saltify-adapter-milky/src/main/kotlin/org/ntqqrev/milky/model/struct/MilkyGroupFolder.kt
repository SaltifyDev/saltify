package org.ntqqrev.milky.model.struct

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MilkyGroupFolder(
    @SerialName("group_id")
    val groupId: Long,

    @SerialName("folder_id")
    val folderId: String,

    @SerialName("parent_folder_id")
    val parentFolderId: String? = null,

    @SerialName("folder_name")
    val folderName: String,

    @SerialName("created_time")
    val createdTime: Long,

    @SerialName("last_modified_time")
    val lastModifiedTime: Long,

    @SerialName("creator_id")
    val creatorId: Long,

    @SerialName("file_count")
    val fileCount: Int,
)