package org.ntqqrev.saltify.action

interface UserAction {
    /**
     * Send a nudge to a user.
     */
    suspend fun sendPrivateNudge(userUin: Long, isSelf: Boolean): Boolean

    /**
     * Send profile like to a user.
     */
    suspend fun sendProfileLike(userUin: Long, count: Int): Boolean
}