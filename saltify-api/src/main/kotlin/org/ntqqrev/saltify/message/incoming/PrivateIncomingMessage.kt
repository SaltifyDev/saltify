package org.ntqqrev.saltify.message.incoming

import org.ntqqrev.saltify.model.User

interface PrivateIncomingMessage : IncomingMessage {
    /**
     * The peer the bot is interacting with.
     */
    val peer: User

    /**
     * Whether the message was sent by the bot itself.
     */
    val isSelf: Boolean

    /**
     * The sequence number of the message in the client's context.
     * This is used to track the order of messages sent by the client.
     * May not be present if the message is fetched from a history.
     */
    val clientSequence: Long?
}