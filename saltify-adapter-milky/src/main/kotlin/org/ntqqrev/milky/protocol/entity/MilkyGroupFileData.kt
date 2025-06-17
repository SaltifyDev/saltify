package org.ntqqrev.milky.protocol.entity

class MilkyGroupFileData(
    val groupId: Long,
    val fileId: String,
    val fileName: String,
    val parentFolderId: String,
    val fileSize: Long,
    val uploadedTime: Long,
    val expireTime: Long? = null,
    val uploaderId: Long,
    val downloadedTimes: Int,
)