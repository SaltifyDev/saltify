package org.ntqqrev.lagrange.internal.packet.oidb

import org.ntqqrev.saltify.protobuf.ProtoMessage
import org.ntqqrev.saltify.protobuf.annotation.ProtoField

class OidbRequest(
    @ProtoField(1) var cmd: Int,
    @ProtoField(2) var subCmd: Int,
    @ProtoField(4) var payload: ByteArray,
) : ProtoMessage()