package org.ntqqrev.saltify.event

import kotlinx.datetime.Instant
import org.ntqqrev.saltify.Context

abstract class AbstractRequestEvent(
    ctx: Context,
    time: Instant,

    /**
     * The id of the request.
     * Can be used to handle the request.
     */
    val requestId: String,

    /**
     * Whether the request is filtered (regarded as unsafe).
     */
    val isFiltered: Boolean,

    /**
     * The uin of the user who sent the request.
     */
    val initiatorUin: Long,

    /**
     * The state of the request.
     */
    val state: RequestState,
) : Event(ctx, time)

enum class RequestState {
    PENDING,
    ACCEPTED,
    REJECTED,
    IGNORED,
}

open class FriendRequestEvent(
    ctx: Context,
    time: Instant,
    requestId: String,
    isFiltered: Boolean,
    initiatorUin: Long,
    state: RequestState,

    /**
     * The additional message sent with the request.
     */
    val comment: String,

    /**
     * How the requestor found you, e.g., via a group, a friend, or a search.
     */
    val via: String,
) : AbstractRequestEvent(ctx, time, requestId, isFiltered, initiatorUin, state)

/**
 * Someone invited you to join the group.
 */
open class GroupInvitationEvent(
    ctx: Context,
    time: Instant,
    requestId: String,
    isFiltered: Boolean,
    initiatorUin: Long,
    state: RequestState,

    /**
     * The uin of the group related to the invitation.
     */
    val groupUin: Long,
) : AbstractRequestEvent(ctx, time, requestId, isFiltered, initiatorUin, state)

abstract class GroupRequestEvent(
    ctx: Context,
    time: Instant,
    requestId: String,
    isFiltered: Boolean,
    initiatorUin: Long,
    state: RequestState,

    /**
     * The uin of the group related to the request.
     */
    val groupUin: Long,

    /**
     * The uin of the user who sent the request.
     */
    val operatorUin: Long?
) : AbstractRequestEvent(ctx, time, requestId, isFiltered, initiatorUin, state)

/**
 * Someone in a group invited another user to join the group.
 */
open class GroupInvitedJoinRequestEvent(
    ctx: Context,
    time: Instant,
    requestId: String,
    isFiltered: Boolean,
    initiatorUin: Long,
    state: RequestState,
    groupUin: Long,
    operatorUin: Long? = null,

    /**
     * The uin of the one being invited to join the group.
     */
    val inviteeUin: Long
) : GroupRequestEvent(ctx, time, requestId, isFiltered, initiatorUin, state, groupUin, operatorUin)

/**
 * Someone requests to join the group.
 */
open class GroupJoinRequestEvent(
    ctx: Context,
    time: Instant,
    requestId: String,
    isFiltered: Boolean,
    initiatorUin: Long,
    state: RequestState,
    groupUin: Long,
    operatorUin: Long? = null,

    /**
     * The additional message sent with the request.
     */
    val comment: String,
) : GroupRequestEvent(ctx, time, requestId, isFiltered, initiatorUin, state, groupUin, operatorUin)