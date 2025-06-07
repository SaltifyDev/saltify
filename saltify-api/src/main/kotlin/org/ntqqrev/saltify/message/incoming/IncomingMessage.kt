package org.ntqqrev.saltify.message.incoming

import kotlinx.datetime.Instant
import org.ntqqrev.saltify.Entity

interface IncomingMessage : Entity {
    /**
     * The sequence number of the message.
     */
    val sequence: Long

    /**
     * The time when the message was sent.
     */
    val time: Instant

    /**
     * The content of the message.
     */
    val segments: List<Segment>
}