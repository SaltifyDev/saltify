package org.ntqqrev.milky.util

import kotlinx.datetime.Instant
import org.ntqqrev.milky.MilkyContext
import org.ntqqrev.milky.protocol.request.*
import org.ntqqrev.saltify.event.*

internal fun MilkyFriendRequestData.toEvent(ctx: MilkyContext) =
    FriendRequestEvent(
        ctx = ctx,
        time = Instant.fromEpochSeconds(time),
        requestId = requestId,
        isFiltered = isFiltered,
        initiatorUin = initiatorId,
        state = state.toSaltifyRequestState(),
        comment = comment ?: "",
        via = via ?: ""
    )

internal fun MilkyGroupRequestData.toEvent(ctx: MilkyContext) =
    when (this) {
        is MilkyGroupJoinRequestData -> GroupJoinRequestEvent(
            ctx = ctx,
            time = Instant.fromEpochSeconds(time),
            requestId = requestId,
            isFiltered = isFiltered,
            initiatorUin = initiatorId,
            state = state.toSaltifyRequestState(),
            groupUin = groupId,
            operatorUin = operatorId,
            comment = comment ?: "",
        )
        is MilkyGroupInviteRequestData -> GroupInvitedJoinRequestEvent(
            ctx = ctx,
            time = Instant.fromEpochSeconds(time),
            requestId = requestId,
            isFiltered = isFiltered,
            initiatorUin = initiatorId,
            state = state.toSaltifyRequestState(),
            groupUin = groupId,
            operatorUin = operatorId,
            inviteeUin = inviteeId,
        )
    }

internal fun MilkyGroupInvitationData.toEvent(ctx: MilkyContext) =
    GroupInvitationEvent(
        ctx = ctx,
        time = Instant.fromEpochSeconds(time),
        requestId = requestId,
        isFiltered = isFiltered,
        initiatorUin = initiatorId,
        state = state.toSaltifyRequestState(),
        groupUin = groupId
    )