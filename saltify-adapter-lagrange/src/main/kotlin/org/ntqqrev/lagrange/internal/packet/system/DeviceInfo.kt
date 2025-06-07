package org.ntqqrev.lagrange.internal.packet.system

import org.ntqqrev.saltify.protobuf.ProtoMessage
import org.ntqqrev.saltify.protobuf.annotation.ProtoField

class DeviceInfo(
    @ProtoField(1) var devName: String,
    @ProtoField(2) var devType: String,
    @ProtoField(3) var osVer: String,
    @ProtoField(4) var brand: String? = null,
    @ProtoField(5) var vendorOsName: String,
) : ProtoMessage()