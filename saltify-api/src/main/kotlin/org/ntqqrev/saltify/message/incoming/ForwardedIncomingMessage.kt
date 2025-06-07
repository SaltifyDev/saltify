package org.ntqqrev.saltify.message.incoming

import org.ntqqrev.saltify.Entity

interface ForwardedIncomingMessage : Entity {
    /**
     * The sender of the original message. Can be fake.
     */
    val senderUin: Long

    /**
     * The sender's nickname in the original message.
     */
    val senderName: String

    /**
     * The content of the message.
     */
    val segments: List<Segment>
}