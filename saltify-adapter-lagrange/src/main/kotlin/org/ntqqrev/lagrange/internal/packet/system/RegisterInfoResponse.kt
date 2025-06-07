package org.ntqqrev.lagrange.internal.packet.system

import org.ntqqrev.saltify.protobuf.ProtoMessage
import org.ntqqrev.saltify.protobuf.annotation.ProtoField

class RegisterInfoResponse(
    @ProtoField(2) var message: String
) : ProtoMessage()