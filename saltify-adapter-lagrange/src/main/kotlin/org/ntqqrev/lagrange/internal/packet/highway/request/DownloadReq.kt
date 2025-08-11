package org.ntqqrev.lagrange.internal.packet.highway.request

import org.ntqqrev.lagrange.internal.packet.highway.IndexNode
import org.ntqqrev.saltify.protobuf.ProtoMessage
import org.ntqqrev.saltify.protobuf.annotation.ProtoField

class DownloadReq(
    @ProtoField(1) var indexNode: IndexNode,
) : ProtoMessage()


