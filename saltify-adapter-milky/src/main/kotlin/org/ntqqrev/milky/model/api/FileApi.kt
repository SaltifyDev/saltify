package org.ntqqrev.milky.model.api

import org.ntqqrev.milky.model.struct.MilkyGroupFileData
import org.ntqqrev.milky.model.struct.MilkyGroupFolderData

internal class MilkyUploadPrivateFileRequest(
    val userId: Long,
    val fileUri: String,
)

internal class MilkyUploadGroupFileRequest(
    val groupId: Long,
    val fileUri: String,
)

internal class MilkyGetPrivateFileDownloadUrlRequest(
    val userId: Long,
    val fileId: String,
)

internal class MilkyGetPrivateFileDownloadUrlResponse(
    val downloadUrl: String,
)

internal class MilkyGetGroupFileDownloadUrlRequest(
    val groupId: Long,
    val fileId: String,
)

internal class MilkyGetGroupFileDownloadUrlResponse(
    val downloadUrl: String,
)

internal class MilkyGetGroupFilesRequest(
    val groupId: Long,
    val parentFolderId: String? = null,
)

internal class MilkyGetGroupFilesResponse(
    val files: List<MilkyGroupFileData>,
    val folders: List<MilkyGroupFolderData>,
)

internal class MilkyMoveGroupFileRequest(
    val groupId: Long,
    val fileId: String,
    val targetFolderId: String? = null,
)

internal class MilkyRenameGroupFileRequest(
    val groupId: Long,
    val fileId: String,
    val newName: String,
)

internal class MilkyDeleteGroupFileRequest(
    val groupId: Long,
    val fileId: String,
)

internal class MilkyCreateGroupFolderRequest(
    val groupId: Long,
    val folderName: String,
)

internal class MilkyCreateGroupFolderResponse(
    val folderId: String,
)

internal class MilkyRenameGroupFolderRequest(
    val groupId: Long,
    val folderId: String,
    val newName: String,
)

internal class MilkyDeleteGroupFolderRequest(
    val groupId: Long,
    val folderId: String,
)