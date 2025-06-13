package org.ntqqrev.milky.model.struct

class MilkyGroupFileData(
    val groupId: Long,
    val fileId: String,
    val fileName: String,
    val parentFolderId: String? = null,
    val fileSize: Long,
    val uploadedTime: Long,
    val expireTime: Long,
    val uploaderId: Long,
    val downloadedTimes: Int,
)