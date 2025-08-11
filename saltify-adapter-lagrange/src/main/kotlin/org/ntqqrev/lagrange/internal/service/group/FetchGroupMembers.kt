package org.ntqqrev.lagrange.internal.service.group

import org.ntqqrev.lagrange.internal.LagrangeClient
import org.ntqqrev.lagrange.internal.packet.oidb.OidbFetchGroupMembersRequest
import org.ntqqrev.lagrange.internal.packet.oidb.OidbFetchGroupMembersResponse
import org.ntqqrev.lagrange.internal.service.OidbService
import org.ntqqrev.lagrange.internal.util.ext.pb

internal object FetchGroupMembers : OidbService<FetchGroupMembers.Req, OidbFetchGroupMembersResponse>(0xfe7, 3) {
    override fun buildOidb(client: LagrangeClient, payload: Req): ByteArray =
        OidbFetchGroupMembersRequest(
            groupUin = payload.groupUin,
            token = payload.token,
        ).pb()

    override fun parseOidb(client: LagrangeClient, payload: ByteArray): OidbFetchGroupMembersResponse =
        payload.pb()

    class Req(
        val groupUin: Long,
        val token: String?
    )
}


