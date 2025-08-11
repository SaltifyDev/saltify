package org.ntqqrev.milky.entity

import kotlinx.datetime.Instant
import org.ntqqrev.milky.MilkyContext
import org.ntqqrev.milky.protocol.entity.MilkyGroupFileData
import org.ntqqrev.milky.protocol.entity.MilkyGroupFolderData
import org.ntqqrev.saltify.model.Group
import org.ntqqrev.saltify.model.group.FileEntry
import org.ntqqrev.saltify.model.group.FolderEntry

class MilkyFileEntry(
    override val ctx: MilkyContext,
    override val group: MilkyGroup,
    data: MilkyGroupFileData,
) : FileEntry {
    override val fileId: String = data.fileId
    override val fileName: String = data.fileName
    override val parentFolderId: String = data.parentFolderId
    override val fileSize: Long = data.fileSize
    override val uploadedTime: Instant = Instant.fromEpochSeconds(data.uploadedTime)
    override val expireTime: Instant? = data.expireTime?.let(Instant::fromEpochSeconds)
    override val uploaderUin: Long = data.uploaderId
    override val downloadedTimes: Long = data.downloadedTimes.toLong()
}

class MilkyFolderEntry(
    override val ctx: MilkyContext,
    override val group: MilkyGroup,
    data: MilkyGroupFolderData,
) : FolderEntry {
    override val folderId: String = data.folderId
    override val parentFolderId: String = data.parentFolderId
    override val folderName: String = data.folderName
    override val createTime: Instant = Instant.fromEpochSeconds(data.createdTime)
    override val lastModifiedTime: Instant = Instant.fromEpochSeconds(data.lastModifiedTime)
    override val creatorUin: Long = data.creatorId
    override val fileCount: Long = data.fileCount.toLong()
}