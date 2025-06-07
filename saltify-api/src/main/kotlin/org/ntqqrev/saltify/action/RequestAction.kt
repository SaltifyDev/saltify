package org.ntqqrev.saltify.action

import org.ntqqrev.saltify.event.FriendRequestEvent
import org.ntqqrev.saltify.event.GroupInvitationEvent
import org.ntqqrev.saltify.event.GroupRequestEvent

interface RequestAction {
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
}