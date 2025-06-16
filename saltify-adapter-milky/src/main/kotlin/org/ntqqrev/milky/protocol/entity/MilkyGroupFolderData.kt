package org.ntqqrev.milky.protocol.entity

class MilkyGroupFolderData(
    val groupId: Long,
    val folderId: String,
    val parentFolderId: String? = null,
    val folderName: String,
    val createdTime: Long,
    val lastModifiedTime: Long,
    val creatorId: Long,
    val fileCount: Int,
)