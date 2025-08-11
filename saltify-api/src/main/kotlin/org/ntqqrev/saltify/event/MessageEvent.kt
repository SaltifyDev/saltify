package org.ntqqrev.saltify.event

import kotlinx.datetime.Instant
import org.ntqqrev.saltify.Context
import org.ntqqrev.saltify.message.MessageScene
import org.ntqqrev.saltify.message.incoming.IncomingMessage
import org.ntqqrev.saltify.model.Friend
import org.ntqqrev.saltify.model.Group
import org.ntqqrev.saltify.model.GroupMember
import org.ntqqrev.saltify.model.User

abstract class AbstractMessageEvent(
    ctx: Context,
    time: Instant,

    /**
     * The message scene where the message was sent.
     */
    val scene: MessageScene,

    /**
     * The uin of the peer (user uin for private chat, group uin for group chat).
     */
    val peerUin: Long,

    /**
     * The sequence number of the message.
     */
    val sequence: Long,
) : Event(ctx, time)

abstract class MessageReceiveEvent(
    ctx: Context,

    /**
     * The message that was received.
     */
    val message: IncomingMessage
) : AbstractMessageEvent(
    ctx,
    message.time,
    message.scene,
    message.peerUin,
    message.sequence
)

open class FriendMessageReceiveEvent(
    ctx: Context,
    message: IncomingMessage,

    /**
     * The friend who sent the message.
     */
    val friend: Friend,
) : MessageReceiveEvent(ctx, message)

open class GroupMessageReceiveEvent(
    ctx: Context,
    message: IncomingMessage,

    /**
     * The group where the message was sent.
     */
    val group: Group,

    /**
     * The group member who sent the message.
     */
    val sender: GroupMember,
) : MessageReceiveEvent(ctx, message)

open class TempMessageReceiveEvent(
    ctx: Context,
    message: IncomingMessage,

    /**
     * The user who sent the message. This is typically a non-friend member of a group.
     */
    val user: User,

    /**
     * The group which the user is a member of, if applicable.
     */
    val group: Group?,
) : MessageReceiveEvent(ctx, message)

open class MessageRecallEvent(
    ctx: Context,
    time: Instant,
    messageScene: MessageScene,
    peerUin: Long,
    sequence: Long,

    /**
     * The group member who recalled the message if the message was sent in a group.
     */
    val operator: GroupMember?,
) : AbstractMessageEvent(ctx, time, messageScene, peerUin, sequence)