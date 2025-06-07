package org.ntqqrev.lagrange.internal.packet.system

import org.ntqqrev.saltify.protobuf.ProtoMessage
import org.ntqqrev.saltify.protobuf.annotation.ProtoField

class SsoHeartbeat(
    @ProtoField(1) var type: Int = 1
) : ProtoMessage()