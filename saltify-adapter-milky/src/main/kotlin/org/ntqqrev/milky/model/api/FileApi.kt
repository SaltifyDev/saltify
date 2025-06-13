package org.ntqqrev.milky.model.api

import org.ntqqrev.milky.model.struct.MilkyGroupFileData
import org.ntqqrev.milky.model.struct.MilkyGroupFolderData

class MilkyUploadPrivateFileRequest(
    val userId: Long,
    val fileUri: String,
)

class MilkyUploadGroupFileRequest(
    val groupId: Long,
    val fileUri: String,
)

class MilkyGetPrivateFileDownloadUrlRequest(
    val userId: Long,
    val fileId: String,
)

class MilkyGetPrivateFileDownloadUrlResponse(
    val downloadUrl: String,
)

class MilkyGetGroupFileDownloadUrlRequest(
    val groupId: Long,
    val fileId: String,
)

class MilkyGetGroupFileDownloadUrlResponse(
    val downloadUrl: String,
)

class MilkyGetGroupFilesRequest(
    val groupId: Long,
    val parentFolderId: String? = null,
)

class MilkyGetGroupFilesResponse(
    val files: List<MilkyGroupFileData>,
    val folders: List<MilkyGroupFolderData>,
)

class MilkyMoveGroupFileRequest(
    val groupId: Long,
    val fileId: String,
    val targetFolderId: String? = null,
)

class MilkyRenameGroupFileRequest(
    val groupId: Long,
    val fileId: String,
    val newName: String,
)

class MilkyDeleteGroupFileRequest(
    val groupId: Long,
    val fileId: String,
)

class MilkyCreateGroupFolderRequest(
    val groupId: Long,
    val folderName: String,
)

class MilkyCreateGroupFolderResponse(
    val folderId: String,
)

class MilkyRenameGroupFolderRequest(
    val groupId: Long,
    val folderId: String,
    val newName: String,
)

class MilkyDeleteGroupFolderRequest(
    val groupId: Long,
    val folderId: String,
)