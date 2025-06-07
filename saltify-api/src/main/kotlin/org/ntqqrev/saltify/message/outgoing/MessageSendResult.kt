package org.ntqqrev.saltify.message.outgoing

import kotlinx.datetime.Instant
import org.ntqqrev.saltify.Entity

interface MessageSendResult : Entity {
    /**
     * The sequence number of the message that was sent.
     */
    val sequence: Long

    /**
     * The time when the message was sent.
     */
    val time: Instant
}