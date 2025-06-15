package org.ntqqrev.saltify.event

import kotlinx.datetime.Instant
import org.ntqqrev.saltify.Context
import org.ntqqrev.saltify.model.Friend

abstract class AbstractFriendEvent(
    ctx: Context,
    time: Instant,

    /**
     * The friend related to the event.
     */
    val friend: Friend
) : Event(ctx, time)

open class FriendNudgeEvent(
    ctx: Context,
    time: Instant,
    friend: Friend,

    /**
     * Whether the nudge is sent by the bot itself.
     */
    val isSelfSend: Boolean,

    /**
     * Whether the nudge is received by the bot itself.
     */
    val isSelfReceive: Boolean,
) : AbstractFriendEvent(ctx, time, friend)

open class FriendFileUploadEvent(
    ctx: Context,
    time: Instant,
    friend: Friend,

    /**
     * The ID of the file that was uploaded.
     */
    val fileId: String,

    /**
     * The name of the file that was uploaded.
     */
    val fileName: String,

    /**
     * The size of the file that was uploaded, in bytes.
     */
    val fileSize: Long,

    /**
     * Whether the file is uploaded by the bot itself.
     */
    val isSelf: Boolean,
) : AbstractFriendEvent(ctx, time, friend)