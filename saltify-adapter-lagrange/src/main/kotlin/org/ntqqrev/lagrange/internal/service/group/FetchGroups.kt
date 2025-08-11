package org.ntqqrev.lagrange.internal.service.group

import org.ntqqrev.lagrange.internal.LagrangeClient
import org.ntqqrev.lagrange.internal.packet.oidb.OidbFetchGroupsRequest
import org.ntqqrev.lagrange.internal.packet.oidb.OidbFetchGroupsResponse
import org.ntqqrev.lagrange.internal.service.NoInputOidbService
import org.ntqqrev.lagrange.internal.util.ext.pb

internal object FetchGroups : NoInputOidbService<OidbFetchGroupsResponse>(0xfe5, 2) {
    override fun buildOidb(client: LagrangeClient, payload: Unit): ByteArray =
        OidbFetchGroupsRequest().pb()

    override fun parseOidb(client: LagrangeClient, payload: ByteArray): OidbFetchGroupsResponse =
        payload.pb()
}


