package org.ntqqrev.lagrange.internal.packet.highway.request

import org.ntqqrev.saltify.protobuf.ProtoMessage
import org.ntqqrev.saltify.protobuf.annotation.ProtoField

class NTV2RichMediaReq(
    @ProtoField(1) var reqHead: MultiMediaReqHead,
    @ProtoField(3) var download: DownloadReq? = null,
) : ProtoMessage()


