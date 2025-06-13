package org.ntqqrev.saltify.action

import org.ntqqrev.saltify.model.group.FileEntry
import org.ntqqrev.saltify.model.group.FileSystemEntry
import java.io.InputStream

interface FileAction {
    /**
     * Upload a file to the specified user.
     * @return The uploaded file ID
     */
    suspend fun uploadPrivateFile(userUin: Long, file: InputStream): String

    /**
     * Get the download URL of a private file.
     * @return The download URL
     */
    suspend fun getPrivateFileDownloadUrl(userUin: Long, fileId: String, fileHash: String): String

    /**
     * Upload a file to the specified group.
     * @return The uploaded file ID
     */
    suspend fun uploadGroupFile(groupUin: Long, file: InputStream, parentFolderId: String = "/"): String

    /**
     * Upload a file to the specified group.
     */
    suspend fun getGroupFiles(groupUin: Long, parentFolderId: String = "/"): List<FileSystemEntry>

    /**
     * Get the download URL of a file in the group.
     * @return The download URL
     */
    suspend fun getGroupFileDownloadUrl(groupUin: Long, fileId: String): String

    /**
     * Move a file in the group file system.
     */
    suspend fun moveGroupFile(groupUin: Long, fileId: String, fromFolderId: String, targetFolderId: String)

    /**
     * Rename a file in the group file system.
     */
    suspend fun renameGroupFile(groupUin: Long, fileId: String, newName: String): FileEntry

    /**
     * Delete a file from the group file system.
     */
    suspend fun deleteGroupFile(groupUin: Long, fileId: String)

    /**
     * Create a folder in the group file system.
     * @return The created folder ID
     */
    suspend fun createGroupFolder(groupUin: Long, folderName: String): String

    /**
     * Rename a folder in the group file system.
     */
    suspend fun renameGroupFolder(groupUin: Long, folderId: String, newName: String)

    /**
     * Delete a folder from the group file system.
     */
    suspend fun deleteGroupFolder(groupUin: Long, folderId: String)
}