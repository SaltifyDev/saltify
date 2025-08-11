package org.ntqqrev.lagrange.internal.packet.highway.request

import org.ntqqrev.lagrange.internal.packet.highway.CommonHead
import org.ntqqrev.saltify.protobuf.ProtoMessage
import org.ntqqrev.saltify.protobuf.annotation.ProtoField

class MultiMediaReqHead(
    @ProtoField(1) var common: CommonHead,
    @ProtoField(2) var scene: SceneInfo,
) : ProtoMessage() {
    class SceneInfo(
        @ProtoField(1) var requestType: Int,
        @ProtoField(2) var businessType: Int,
        @ProtoField(3) var sceneType: Int,
        @ProtoField(5) var group: Group? = null,
    ) : ProtoMessage() {
        class Group : ProtoMessage()
    }
}


