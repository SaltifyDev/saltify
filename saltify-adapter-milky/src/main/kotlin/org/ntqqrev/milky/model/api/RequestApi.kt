package org.ntqqrev.milky.model.api

import org.ntqqrev.milky.model.request.MilkyFriendRequestData
import org.ntqqrev.milky.model.request.MilkyGroupInvitationData
import org.ntqqrev.milky.model.request.MilkyGroupRequestData

class MilkyGetFriendRequestsRequest(
    val limit: Int,
)

class MilkyGetFriendRequestsResponse(
    val requests: List<MilkyFriendRequestData>,
)

class MilkyGetGroupRequestsRequest(
    val limit: Int,
)

class MilkyGetGroupRequestsResponse(
    val requests: List<MilkyGroupRequestData>,
)

class MilkyGetGroupInvitationsRequest(
    val limit: Int,
)

class MilkyGetGroupInvitationsResponse(
    val invitations: List<MilkyGroupInvitationData>,
)

class MilkyAcceptRequestRequest(
    val requestId: String,
)

class MilkyRejectRequestRequest(
    val requestId: String,
    val reason: String? = null,
)