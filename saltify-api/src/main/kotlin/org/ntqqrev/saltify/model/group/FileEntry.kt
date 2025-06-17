package org.ntqqrev.saltify.model.group

import kotlinx.datetime.Instant

/**
 * Represents a file entry in the group file system.
 */
interface FileEntry : FileSystemEntry {
    /**
     * The ID of the file.
     */
    val fileId: String

    /**
     * The name of the file.
     */
    val fileName: String

    /**
     * The parent directory ID of the file.
     */
    val parentFolderId: String

    /**
     * The size of the file in bytes.
     */
    val fileSize: Long

    /**
     * The time when the file was uploaded.
     */
    val uploadedTime: Instant

    /**
     * The time when the file will expire.
     */
    val expireTime: Instant?

    /**
     * The uin of the uploader.
     * Not using `GroupMember` object because the uploader may be no longer a member of the group.
     */
    val uploaderUin: Long

    /**
     * The number of times the file has been downloaded.
     */
    val downloadedTimes: Long
}