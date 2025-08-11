package org.ntqqrev.lagrange.internal.packet.highway

import org.ntqqrev.saltify.protobuf.ProtoMessage
import org.ntqqrev.saltify.protobuf.annotation.ProtoField

class CommonHead(
    @ProtoField(1) var requestId: Int,
    @ProtoField(2) var command: Int,
) : ProtoMessage()


