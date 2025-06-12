package org.ntqqrev.milky.model.api

import org.ntqqrev.milky.model.request.MilkyFriendRequest
import org.ntqqrev.milky.model.request.MilkyGroupInvitation
import org.ntqqrev.milky.model.request.MilkyGroupRequest

internal class MilkyGetFriendRequestsRequest(
    val limit: Int,
)

internal class MilkyGetFriendRequestsResponse(
    val requests: List<MilkyFriendRequest>,
)

internal class MilkyGetGroupRequestsRequest(
    val limit: Int,
)

internal class MilkyGetGroupRequestsResponse(
    val requests: List<MilkyGroupRequest>,
)

internal class MilkyGetGroupInvitationsRequest(
    val limit: Int,
)

internal class MilkyGetGroupInvitationsResponse(
    val invitations: List<MilkyGroupInvitation>,
)

internal class MilkyAcceptRequestRequest(
    val requestId: String,
)

internal class MilkyRejectRequestRequest(
    val requestId: String,
    val reason: String? = null,
)