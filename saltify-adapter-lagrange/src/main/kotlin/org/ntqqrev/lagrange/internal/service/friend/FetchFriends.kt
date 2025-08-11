package org.ntqqrev.lagrange.internal.service.friend

import org.ntqqrev.lagrange.internal.LagrangeClient
import org.ntqqrev.lagrange.internal.packet.oidb.OidbFetchFriendsRequest
import org.ntqqrev.lagrange.internal.packet.oidb.OidbFetchFriendsResponse
import org.ntqqrev.lagrange.internal.packet.oidb.UinBody
import org.ntqqrev.lagrange.internal.service.OidbService
import org.ntqqrev.lagrange.internal.util.ext.pb

internal object FetchFriends : OidbService<FetchFriends.Req, FetchFriends.Resp>(0xfd4, 1) {
    override fun buildOidb(client: LagrangeClient, payload: Req): ByteArray =
        OidbFetchFriendsRequest(nextUin = UinBody(payload.nextUin)).pb()

    override fun parseOidb(client: LagrangeClient, payload: ByteArray): Resp {
        val rawResp = payload.pb<OidbFetchFriendsResponse>()
        return Resp(
            entries = rawResp.friends.map { entry ->
                val properties = entry.additional[1]?.properties ?: emptyMap()
                Entry(
                    uin = entry.uin,
                    uid = entry.uid,
                    nickname = properties[20002],
                    remark = properties[103],
                    signature = properties[102],
                    qid = properties[27394],
                    category = entry.category ?: 0,
                )
            },
            categories = rawResp.categories.associate { it.id to it.name },
            nextUin = rawResp.next?.uin,
        )
    }

    class Req(
        val nextUin: Long?,
    )

    class Resp(
        val entries: List<Entry>,
        val categories: Map<Int, String>,
        val nextUin: Long?
    )

    class Entry(
        val uin: Long,
        val uid: String,
        val nickname: String?,
        val remark: String?,
        val signature: String?,
        val qid: String?,
        val category: Int,
    )
}


