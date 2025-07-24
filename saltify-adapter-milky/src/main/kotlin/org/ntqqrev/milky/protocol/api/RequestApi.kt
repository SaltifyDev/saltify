package org.ntqqrev.milky.protocol.api

import org.ntqqrev.milky.protocol.request.MilkyFriendRequestData
import org.ntqqrev.milky.protocol.request.MilkyGroupInvitationData
import org.ntqqrev.milky.protocol.request.MilkyGroupRequestData

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

class MilkyAcceptFriendRequestRequest(
    val requestId: String,
)

class MilkyRejectFriendRequestRequest(
    val requestId: String,
    val reason: String? = null,
)

class MilkyAcceptGroupRequestRequest(
    val requestId: String,
)

class MilkyRejectGroupRequestRequest(
    val requestId: String,
    val reason: String? = null,
)

class MilkyAcceptGroupInvitationRequest(
    val requestId: String,
)

class MilkyRejectGroupInvitationRequest(
    val requestId: String,
)