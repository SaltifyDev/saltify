package org.ntqqrev.lagrange.internal.service.highway

import org.ntqqrev.lagrange.internal.LagrangeClient
import org.ntqqrev.lagrange.internal.packet.highway.CommonHead
import org.ntqqrev.lagrange.internal.packet.highway.IndexNode
import org.ntqqrev.lagrange.internal.packet.highway.request.DownloadReq
import org.ntqqrev.lagrange.internal.packet.highway.request.MultiMediaReqHead
import org.ntqqrev.lagrange.internal.packet.highway.request.NTV2RichMediaReq
import org.ntqqrev.lagrange.internal.packet.highway.response.NTV2RichMediaResp
import org.ntqqrev.lagrange.internal.service.OidbService
import org.ntqqrev.lagrange.internal.util.ext.pb

internal object GetGroupImageUrl : OidbService<IndexNode, String>(0x11c4, 200) {
    override fun buildOidb(client: LagrangeClient, payload: IndexNode): ByteArray =
        NTV2RichMediaReq(
            reqHead = MultiMediaReqHead(
                common = CommonHead(
                    requestId = 1,
                    command = 200,
                ),
                scene = MultiMediaReqHead.SceneInfo(
                    requestType = 2,
                    businessType = 1,
                    sceneType = 2,
                    group = MultiMediaReqHead.SceneInfo.Group(),
                ),
            ),
            download = DownloadReq(payload)
        ).pb()

    override fun parseOidb(client: LagrangeClient, payload: ByteArray): String =
        payload.pb<NTV2RichMediaResp>().download
            ?.let { "https://${it.info!!.domain}${it.info!!.urlPath}${it.rKeyParam!!}" }
            ?: throw IllegalStateException("Failed to parse group image URL")
}


