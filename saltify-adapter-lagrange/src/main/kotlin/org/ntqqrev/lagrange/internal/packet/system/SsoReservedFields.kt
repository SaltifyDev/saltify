package org.ntqqrev.lagrange.internal.packet.system

import org.ntqqrev.saltify.protobuf.ProtoMessage
import org.ntqqrev.saltify.protobuf.annotation.ProtoField

class SsoReservedFields(
    @ProtoField(15) var trace: String,
    @ProtoField(16) var uid: String?,
    @ProtoField(24) var secureInfo: SecureInfo?,
) : ProtoMessage() {
    class SecureInfo(
        @ProtoField(1) var sign: ByteArray,
        @ProtoField(2) var token: ByteArray,
        @ProtoField(3) var extra: ByteArray,
    ) : ProtoMessage()
}