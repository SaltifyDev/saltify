package org.ntqqrev.saltify.action

import org.ntqqrev.saltify.message.MessageScene
import org.ntqqrev.saltify.message.incoming.ForwardedIncomingMessage
import org.ntqqrev.saltify.message.incoming.GroupIncomingMessage
import org.ntqqrev.saltify.message.incoming.IncomingMessage
import org.ntqqrev.saltify.message.incoming.PrivateIncomingMessage
import org.ntqqrev.saltify.message.outgoing.GroupMessageBuilder
import org.ntqqrev.saltify.message.outgoing.MessageSendResult
import org.ntqqrev.saltify.message.outgoing.PrivateMessageBuilder

interface MessageAction {
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
}