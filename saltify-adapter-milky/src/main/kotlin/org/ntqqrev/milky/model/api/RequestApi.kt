package org.ntqqrev.milky.model.api

import org.ntqqrev.milky.model.request.MilkyFriendRequestData
import org.ntqqrev.milky.model.request.MilkyGroupInvitationData
import org.ntqqrev.milky.model.request.MilkyGroupRequestData

internal class MilkyGetFriendRequestsRequest(
    val limit: Int,
)

internal class MilkyGetFriendRequestsResponse(
    val requests: List<MilkyFriendRequestData>,
)

internal class MilkyGetGroupRequestsRequest(
    val limit: Int,
)

internal class MilkyGetGroupRequestsResponse(
    val requests: List<MilkyGroupRequestData>,
)

internal class MilkyGetGroupInvitationsRequest(
    val limit: Int,
)

internal class MilkyGetGroupInvitationsResponse(
    val invitations: List<MilkyGroupInvitationData>,
)

internal class MilkyAcceptRequestRequest(
    val requestId: String,
)

internal class MilkyRejectRequestRequest(
    val requestId: String,
    val reason: String? = null,
)