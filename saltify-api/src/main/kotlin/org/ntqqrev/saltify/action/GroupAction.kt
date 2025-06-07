package org.ntqqrev.saltify.action

import org.ntqqrev.saltify.model.group.Announcement
import java.io.InputStream

interface GroupAction {
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
    suspend fun setGroupAvatar(groupUin: Long, image: InputStream)

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
    suspend fun sendGroupAnnouncement(groupUin: Long, content: String, image: InputStream? = null)

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
}