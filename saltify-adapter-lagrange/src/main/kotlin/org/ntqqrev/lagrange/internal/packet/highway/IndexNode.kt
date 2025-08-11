package org.ntqqrev.lagrange.internal.packet.highway

import org.ntqqrev.saltify.protobuf.ProtoMessage
import org.ntqqrev.saltify.protobuf.annotation.ProtoField

class IndexNode(
    @ProtoField(2) var fileUuid: String,
    @ProtoField(4) var ttl: Int,
) : ProtoMessage()


