package org.ntqqrev.lagrange.internal.packet.system

import org.ntqqrev.saltify.protobuf.ProtoMessage
import org.ntqqrev.saltify.protobuf.annotation.ProtoField

class RegisterInfo(
    @ProtoField(1) var guid: String,
    @ProtoField(2) var kickPc: Boolean = false,
    @ProtoField(3) var currentVersion: String,
    @ProtoField(4) var isFirstRegisterProxyOnline: Boolean = false,
    @ProtoField(5) var localeId: Int = 0,
    @ProtoField(6) var device: DeviceInfo,
    @ProtoField(7) var setMute: Int = 0,
    @ProtoField(8) var registerVendorType: Int = 0,
    @ProtoField(9) var regType: Int = 0,
    @ProtoField(10) var businessInfo: OnlineBusinessInfo = OnlineBusinessInfo(
        notifySwitch = 1,
        bindUinNotifySwitch = 1,
    ),
    @ProtoField(11) var batteryStatus: Int = 0,
) : ProtoMessage()