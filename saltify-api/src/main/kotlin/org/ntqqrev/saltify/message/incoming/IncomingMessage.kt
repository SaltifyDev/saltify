package org.ntqqrev.saltify.message.incoming

import kotlinx.datetime.Instant
import org.ntqqrev.saltify.Entity
import org.ntqqrev.saltify.message.MessageScene

interface IncomingMessage : Entity {
    /**
     * The scene where the message was sent.
     */
    val scene: MessageScene

    /**
     * The uin of the peer (user uin for private chat, group uin for group chat).
     */
    val peerUin: Long

    /**
     * The sequence number of the message.
     */
    val sequence: Long

    /**
     * The time when the message was sent.
     */
    val time: Instant

    /**
     * The uin of the sender of the message.
     */
    val senderUin: Long

    /**
     * The display name of the sender of the message.
     *
     * For private / temp messages, this is:
     * - the remark set by the bot account
     * - the nickname of the user, if no remark is set
     *
     * For group messages, this is:
     * - the remark set by the bot account
     * - the card set by the group member, if no remark is set
     * - the nickname of the group member, if no remark or card is set
     */
    val senderName: String

    /**
     * The content of the message.
     */
    val segments: List<Segment>
}
