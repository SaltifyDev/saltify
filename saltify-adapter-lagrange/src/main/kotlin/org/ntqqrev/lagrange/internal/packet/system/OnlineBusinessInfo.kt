package org.ntqqrev.lagrange.internal.packet.system

import org.ntqqrev.saltify.protobuf.ProtoMessage
import org.ntqqrev.saltify.protobuf.annotation.ProtoField

class OnlineBusinessInfo(
    @ProtoField(1) var notifySwitch: Int,
    @ProtoField(2) var bindUinNotifySwitch: Int,
) : ProtoMessage()