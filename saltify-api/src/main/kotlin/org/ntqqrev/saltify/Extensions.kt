package org.ntqqrev.saltify

import org.ntqqrev.saltify.event.AbstractRequestEvent
import org.ntqqrev.saltify.event.FriendFileUploadEvent
import org.ntqqrev.saltify.message.MessageScene
import org.ntqqrev.saltify.message.incoming.ForwardSegment
import org.ntqqrev.saltify.message.incoming.GroupIncomingMessage
import org.ntqqrev.saltify.message.incoming.ResourceLikeSegment
import org.ntqqrev.saltify.message.outgoing.GroupMessageBuilder
import org.ntqqrev.saltify.message.outgoing.PrivateMessageBuilder
import org.ntqqrev.saltify.message.outgoing.ResourceLocation
import org.ntqqrev.saltify.model.Friend
import org.ntqqrev.saltify.model.Group
import org.ntqqrev.saltify.model.GroupMember
import org.ntqqrev.saltify.model.group.Announcement
import org.ntqqrev.saltify.model.group.FileEntry
import org.ntqqrev.saltify.model.group.FolderEntry

suspend fun Group.getAllMembers() =
    ctx.getAllGroupMembers(uin)

suspend fun Group.getMember(memberUin: Long) =
    ctx.getGroupMember(uin, memberUin)

suspend fun Friend.sendMessage(builder: PrivateMessageBuilder.() -> Unit) =
    ctx.sendPrivateMessage(uin, builder)

suspend fun Friend.sendMessage(message: String) =
    sendMessage { text(message) }

suspend fun Group.sendMessage(builder: GroupMessageBuilder.() -> Unit) =
    ctx.sendGroupMessage(uin, builder)

suspend fun Group.sendMessage(message: String) =
    sendMessage { text(message) }

suspend fun Friend.getMessage(sequence: Long) =
    ctx.getMessage(MessageScene.FRIEND, uin, sequence)

suspend fun Group.getMessage(sequence: Long) =
    ctx.getMessage(MessageScene.GROUP, uin, sequence)

suspend fun Friend.getHistoryMessages(
    startSequence: Long? = null,
    isBackward: Boolean = true,
    limit: Int = 20
) = ctx.getHistoryMessages(MessageScene.FRIEND, uin, startSequence, isBackward, limit)

suspend fun Group.getHistoryMessages(
    startSequence: Long? = null,
    isBackward: Boolean = true,
    limit: Int = 20
) = ctx.getHistoryMessages(MessageScene.GROUP, uin, startSequence, isBackward, limit)

suspend fun ResourceLikeSegment.getTempUrl() =
    message.ctx.getResourceTempUrl(resourceId)

suspend fun ForwardSegment.getMessages() =
    message.ctx.getForwardedMessages(forwardId)

suspend fun GroupIncomingMessage.recall() =
    ctx.recallGroupMessage(group.uin, sequence)

suspend fun Friend.sendNudge(isSelf: Boolean = false) =
    ctx.sendPrivateNudge(uin, isSelf)

suspend fun Friend.sendProfileLike(count: Int) =
    ctx.sendProfileLike(uin, count)

suspend fun Group.getAnnouncements() =
    ctx.getGroupAnnouncements(uin)

suspend fun Group.setName(newName: String) =
    ctx.setGroupName(uin, newName)

suspend fun Group.setAvatar(image: ResourceLocation) =
    ctx.setGroupAvatar(uin, image)

suspend fun GroupMember.setCard(newCard: String) =
    ctx.setGroupMemberCard(group.uin, uin, newCard)

suspend fun GroupMember.setSpecialTitle(newTitle: String) =
    ctx.setGroupMemberSpecialTitle(group.uin, uin, newTitle)

suspend fun GroupMember.setAdmin(isPromote: Boolean) =
    ctx.setGroupMemberAdmin(group.uin, uin, isPromote)

suspend fun GroupMember.setMute(duration: Int) =
    ctx.setGroupMemberMute(group.uin, uin, duration)

suspend fun Group.setWholeMute(isMute: Boolean) =
    ctx.setGroupWholeMute(uin, isMute)

suspend fun GroupMember.kick(isPermanent: Boolean) =
    ctx.kickGroupMember(group.uin, uin, isPermanent)

suspend fun Group.sendAnnouncement(content: String, image: ResourceLocation? = null) =
    ctx.sendGroupAnnouncement(uin, content, image)

suspend fun Announcement.delete() =
    group.ctx.deleteGroupAnnouncement(group.uin, announcementId)

suspend fun Group.quit() =
    ctx.quitGroup(uin)

suspend fun GroupMember.sendNudge() =
    ctx.sendGroupNudge(group.uin, uin)

suspend fun GroupIncomingMessage.sendReaction(reactionId: String, isAdd: Boolean) =
    ctx.sendGroupMessageReaction(group.uin, sequence, reactionId, isAdd)

suspend fun AbstractRequestEvent.accept() =
    ctx.acceptRequest(requestId)

suspend fun AbstractRequestEvent.reject(reason: String? = null) =
    ctx.rejectRequest(requestId, reason)

suspend fun Friend.uploadFile(file: ResourceLocation) =
    ctx.uploadPrivateFile(uin, file)

suspend fun FriendFileUploadEvent.getDownloadUrl() =
    ctx.getPrivateFileDownloadUrl(friend.uin, fileId)

suspend fun Group.uploadFile(file: ResourceLocation, parentFolderId: String = "/") =
    ctx.uploadGroupFile(uin, file, parentFolderId)

suspend fun Group.getFiles(parentFolderId: String = "/") =
    ctx.getGroupFiles(uin, parentFolderId)

suspend fun FolderEntry.getFiles() =
    ctx.getGroupFiles(group.uin, folderId)

suspend fun FileEntry.getDownloadUrl() =
    ctx.getGroupFileDownloadUrl(group.uin, fileId)

suspend fun FileEntry.moveTo(target: FolderEntry) =
    ctx.moveGroupFile(group.uin, fileId, parentFolderId, target.folderId)

suspend fun FileEntry.renameTo(newName: String) =
    ctx.renameGroupFile(group.uin, fileId, newName)

suspend fun FileEntry.delete() =
    ctx.deleteGroupFile(group.uin, fileId)

suspend fun Group.createFolder(folderName: String) =
    ctx.createGroupFolder(uin, folderName)

suspend fun FolderEntry.renameTo(newName: String) =
    ctx.renameGroupFolder(group.uin, folderId, newName)

suspend fun FolderEntry.delete() =
    ctx.deleteGroupFolder(group.uin, folderId)