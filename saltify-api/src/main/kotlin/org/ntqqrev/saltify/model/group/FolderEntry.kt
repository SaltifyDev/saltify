package org.ntqqrev.saltify.model.group

import kotlinx.datetime.Instant

/**
 * Represents a folder entry in the group file system.
 */
interface FolderEntry : FileSystemEntry {
    /**
     * The ID of the folder.
     */
    val folderId: String

    /**
     * The ID of the parent folder.
     */
    val parentFolderId: String

    /**
     * The name of the folder.
     */
    val folderName: String

    /**
     * The time when the folder was created.
     */
    val createTime: Instant

    /**
     * The time when the folder was last modified.
     */
    val lastModifiedTime: Instant

    /**
     * The uin of the creator.
     * Not using `GroupMember` object because the creator may be no longer a member of the group.
     */
    val creatorUin: Long

    /**
     * The total number of files in the folder.
     */
    val fileCount: Long
}