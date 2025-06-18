package org.ntqqrev.saltify.message.incoming

import kotlinx.datetime.Instant
import org.ntqqrev.saltify.Entity
import org.ntqqrev.saltify.model.Group
import org.ntqqrev.saltify.model.GroupMember
import org.ntqqrev.saltify.model.User

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
     * The sender of the message.
     */
    val sender: User

    /**
     * The content of the message.
     */
    val segments: List<Segment>
}

interface PrivateIncomingMessage : IncomingMessage {
    /**
     * The peer the bot is interacting with.
     */
    val peer: User
}

interface GroupIncomingMessage : IncomingMessage {
    /**
     * The group where the message was sent.
     */
    val group: Group

    /**
     * The member who sent the message.
     */
    override val sender: GroupMember
}