package org.ntqqrev.lagrange.internal.packet.highway.response

import org.ntqqrev.saltify.protobuf.ProtoMessage
import org.ntqqrev.saltify.protobuf.annotation.ProtoField

class NTV2RichMediaResp(
    @ProtoField(2) var download: Download? = null,
) : ProtoMessage() {
    class Download(
        @ProtoField(1) var info: Info? = null,
        @ProtoField(2) var rKeyParam: String? = null,
    ) : ProtoMessage() {
        class Info(
            @ProtoField(2) var domain: String,
            @ProtoField(4) var urlPath: String,
        ) : ProtoMessage()
    }
}


