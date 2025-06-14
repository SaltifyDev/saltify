package org.ntqqrev.saltify.message.incoming

import kotlinx.datetime.Instant
import org.ntqqrev.saltify.Entity

interface ForwardedIncomingMessage : Entity {
    /**
     * The sender's nickname in the original message.
     */
    val senderName: String

    /**
     * The URL of the sender's avatar in the original message.
     */
    val senderAvatarLink: String

    /**
     * The time when the original message was sent.
     */
    val time: Instant

    /**
     * The content of the message.
     */
    val segments: List<Segment>
}