package org.ntqqrev.saltify

import org.ntqqrev.saltify.event.FriendRequestEvent
import org.ntqqrev.saltify.event.GroupInvitationEvent
import org.ntqqrev.saltify.event.GroupRequestEvent
import org.ntqqrev.saltify.message.MessageScene
import org.ntqqrev.saltify.message.incoming.ForwardedIncomingMessage
import org.ntqqrev.saltify.message.incoming.IncomingMessage
import org.ntqqrev.saltify.message.outgoing.GroupMessageBuilder
import org.ntqqrev.saltify.message.outgoing.MessageSendResult
import org.ntqqrev.saltify.message.outgoing.PrivateMessageBuilder
import org.ntqqrev.saltify.message.outgoing.ResourceLocation
import org.ntqqrev.saltify.model.Friend
import org.ntqqrev.saltify.model.Group
import org.ntqqrev.saltify.model.GroupMember
import org.ntqqrev.saltify.model.group.Announcement
import org.ntqqrev.saltify.model.group.FileEntry
import org.ntqqrev.saltify.model.group.FileSystemEntry

/**
 * The context object, which represents the bot itself.
 */
interface Context {
    /**
     * The state of the context.
     */
    val state: State

    enum class State {
        /**
         * `(O_<)`
         * The context is newly created and not yet started.
         */
        INITIALIZED,

        /**
         * `(OwO)`
         * The context is started and ready to handle requests.
         */
        STARTED,

        /**
         * `(-_-) zzz`
         * The context is stopped and not running.
         * Note that a Context instance is not reusable after it is stopped.
         * A new Context instance is created whenever a new bot is started.
         */
        STOPPED,

        /**
         * `(>_<)`
         * The context encountered an issue and is temporarily interrupted.
         * This state is usually recoverable, and the context can be resumed.
         */
        INTERRUPTED,

        /**
         * `(T_T)`
         * The context encountered an error and is unexpectedly terminated.
         * This state is usually not recoverable.
         */
        TERMINATED,
    }

    /**
     * The starting logic of the context.
     */
    suspend fun start()

    /**
     * The stopping logic of the context.
     */
    suspend fun stop()

    /**
     * Get the login information.
     * @return A pair of (uin, nickname).
     */
    suspend fun getLoginInfo(): Pair<Long, String>

    /**
     * Get all friends.
     */
    suspend fun getAllFriends(cacheFirst: Boolean = true): Iterable<Friend>

    /**
     * Get a friend by its uin.
     */
    suspend fun getFriend(friendUin: Long, cacheFirst: Boolean = true): Friend?

    /**
     * Get all groups.
     */
    suspend fun getAllGroups(cacheFirst: Boolean = true): Iterable<Group>

    /**
     * Get a group by its uin.
     */
    suspend fun getGroup(groupUin: Long, cacheFirst: Boolean = true): Group?

    /**
     * Get all group members from a group.
     */
    suspend fun getAllGroupMembers(groupUin: Long, cacheFirst: Boolean = true): Iterable<GroupMember>

    /**
     * Get a group member by its uin.
     */
    suspend fun getGroupMember(groupUin: Long, memberUin: Long, cacheFirst: Boolean = true): GroupMember?

    /**
     * Send a private message to a user.
     */
    suspend fun sendPrivateMessage(userUin: Long, builder: PrivateMessageBuilder.() -> Unit): MessageSendResult

    /**
     * Send a group message to a group.
     */
    suspend fun sendGroupMessage(groupUin: Long, builder: GroupMessageBuilder.() -> Unit): MessageSendResult

    /**
     * Get a message by its sequence number.
     */
    suspend fun getMessage(messageScene: MessageScene, peerId: Long, sequence: Long): IncomingMessage?

    /**
     * Get history messages with a starting sequence number and a limit.
     */
    suspend fun getHistoryMessages(
        messageScene: MessageScene,
        peerId: Long,
        startSequence: Long? = null,
        isBackward: Boolean = true,
        limit: Int = 20
    ): List<IncomingMessage>

    /**
     * Get a URL for a resource.
     * @return The temporary URL of the resource
     */
    suspend fun getResourceTempUrl(resourceId: String): String

    /**
     * Get the forward messages from ID.
     */
    suspend fun getForwardedMessages(forwardId: String): List<ForwardedIncomingMessage>

    /**
     * Recall a private message.
     */
    suspend fun recallPrivateMessage(userUin: Long, sequence: Long)

    /**
     * Recall a group message.
     */
    suspend fun recallGroupMessage(groupUin: Long, sequence: Long)

    /**
     * Send a nudge to a user.
     */
    suspend fun sendPrivateNudge(userUin: Long, isSelf: Boolean)

    /**
     * Send profile like to a user.
     */
    suspend fun sendProfileLike(userUin: Long, count: Int)

    /**
     * Get all announcements from a group.
     */
    suspend fun getGroupAnnouncements(groupUin: Long): List<Announcement>

    /**
     * Set the name of a group.
     */
    suspend fun setGroupName(groupUin: Long, name: String)

    /**
     * Set the avatar of a group.
     */
    suspend fun setGroupAvatar(groupUin: Long, image: ResourceLocation)

    /**
     * Set the card of a group member.
     */
    suspend fun setGroupMemberCard(groupUin: Long, memberUin: Long, card: String)

    /**
     * Set the special title of a group member.
     * @param title The special title. If it is empty, the special title will be removed.
     */
    suspend fun setGroupMemberSpecialTitle(groupUin: Long, memberUin: Long, title: String)

    /**
     * Set the group member to be an admin or not.
     * @param isPromote true to promote the member to admin, false to demote.
     */
    suspend fun setGroupMemberAdmin(groupUin: Long, memberUin: Long, isPromote: Boolean)

    /**
     * Mute a group member for a specific duration, or unmute them if the duration is 0.
     * @param duration The duration in seconds. 0 means unmuting.
     */
    suspend fun setGroupMemberMute(groupUin: Long, memberUin: Long, duration: Int)

    /**
     * Set mute / unmute on all non-admin members in a group.
     */
    suspend fun setGroupWholeMute(groupUin: Long, isMute: Boolean)

    /**
     * Kick a group member.
     * @param isPermanent true to permanently kick the member (do not accept them back);
     * false to temporarily kick the member (expect later requests).
     */
    suspend fun kickGroupMember(groupUin: Long, memberUin: Long, isPermanent: Boolean)

    /**
     * Send an announcement to a group.
     */
    suspend fun sendGroupAnnouncement(groupUin: Long, content: String, image: ResourceLocation? = null)

    /**
     * Delete an announcement from a group.
     */
    suspend fun deleteGroupAnnouncement(groupUin: Long, announcementId: String)

    /**
     * Quit a group.
     */
    suspend fun quitGroup(groupUin: Long)

    /**
     * Send a nudge to a group.
     */
    suspend fun sendGroupNudge(groupUin: Long, memberUin: Long)

    /**
     * Add / delete a reaction to a group message.
     */
    suspend fun sendGroupMessageReaction(groupUin: Long, sequence: Long, reactionId: String, isAdd: Boolean)

    /**
     * Get the recent friend requests.
     */
    suspend fun getRecentFriendRequests(limit: Int = 20): List<FriendRequestEvent>

    /**
     * Get the recent group requests.
     */
    suspend fun getRecentGroupRequests(limit: Int = 20): List<GroupRequestEvent>

    /**
     * Get the recent group invitations.
     */
    suspend fun getRecentGroupInvitations(limit: Int = 20): List<GroupInvitationEvent>

    /**
     * Accept the request with the given id.
     */
    suspend fun acceptRequest(requestId: String)

    /**
     * Reject the request with the given id, using the given reason or no reason.
     */
    suspend fun rejectRequest(requestId: String, reason: String? = null)

    /**
     * Upload a file to the specified user.
     * @return The uploaded file ID
     */
    suspend fun uploadPrivateFile(userUin: Long, file: ResourceLocation): String

    /**
     * Get the download URL of a private file.
     * @return The download URL
     */
    suspend fun getPrivateFileDownloadUrl(userUin: Long, fileId: String): String

    /**
     * Upload a file to the specified group.
     * @return The uploaded file ID
     */
    suspend fun uploadGroupFile(groupUin: Long, file: ResourceLocation, parentFolderId: String = "/"): String

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