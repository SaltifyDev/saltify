package org.ntqqrev.lagrange.internal.packet.oidb

import org.ntqqrev.saltify.protobuf.ProtoMessage
import org.ntqqrev.saltify.protobuf.annotation.ProtoField

class OidbFetchGroupsRequest(
    @ProtoField(1) var config: Config = Config(),
) : ProtoMessage() {

    class Config(
        @ProtoField(1) var config1: Config1 = Config1(),
        @ProtoField(2) var config2: Config2 = Config2(),
        @ProtoField(3) var config3: Config3 = Config3(),
    ) : ProtoMessage() {

        class Config1(
            @ProtoField(1) var groupOwner: Boolean = true,
            @ProtoField(2) var field2: Boolean = true,
            @ProtoField(3) var memberMax: Boolean = true,
            @ProtoField(4) var memberCount: Boolean = true,
            @ProtoField(5) var groupName: Boolean = true,
            @ProtoField(8) var field8: Boolean = true,
            @ProtoField(9) var field9: Boolean = true,
            @ProtoField(10) var field10: Boolean = true,
            @ProtoField(11) var field11: Boolean = true,
            @ProtoField(12) var field12: Boolean = true,
            @ProtoField(13) var field13: Boolean = true,
            @ProtoField(14) var field14: Boolean = true,
            @ProtoField(15) var field15: Boolean = true,
            @ProtoField(16) var field16: Boolean = true,
            @ProtoField(17) var field17: Boolean = true,
            @ProtoField(18) var field18: Boolean = true,
            @ProtoField(19) var question: Boolean = true,
            @ProtoField(20) var field20: Boolean = true,
            @ProtoField(22) var field22: Boolean = true,
            @ProtoField(23) var field23: Boolean = true,
            @ProtoField(24) var field24: Boolean = true,
            @ProtoField(25) var field25: Boolean = true,
            @ProtoField(26) var field26: Boolean = true,
            @ProtoField(27) var field27: Boolean = true,
            @ProtoField(28) var field28: Boolean = true,
            @ProtoField(29) var field29: Boolean = true,
            @ProtoField(30) var field30: Boolean = true,
            @ProtoField(31) var field31: Boolean = true,
            @ProtoField(32) var field32: Boolean = true,
            @ProtoField(5001) var field5001: Boolean = true,
            @ProtoField(5002) var field5002: Boolean = true,
            @ProtoField(5003) var field5003: Boolean = true,
        ) : ProtoMessage()

        class Config2(
            @ProtoField(1) var field1: Boolean = true,
            @ProtoField(2) var field2: Boolean = true,
            @ProtoField(3) var field3: Boolean = true,
            @ProtoField(4) var field4: Boolean = true,
            @ProtoField(5) var field5: Boolean = true,
            @ProtoField(6) var field6: Boolean = true,
            @ProtoField(7) var field7: Boolean = true,
            @ProtoField(8) var field8: Boolean = true,
        ) : ProtoMessage()

        class Config3(
            @ProtoField(5) var field5: Boolean = true,
            @ProtoField(6) var field6: Boolean = true,
        ) : ProtoMessage()
    }
}


class OidbFetchGroupsResponse(
    @ProtoField(2) var entries: List<Entry>,
) : ProtoMessage() {

    class Entry(
        @ProtoField(3) var groupUin: Long,
        @ProtoField(4) var info: Info,
        @ProtoField(5) var customInfo: CustomInfo?,
    ) : ProtoMessage() {

        class Info(
            @ProtoField(1) var groupOwner: Member?,
            @ProtoField(2) var createdTime: Long,
            @ProtoField(3) var memberMax: Int,
            @ProtoField(4) var memberCount: Int,
            @ProtoField(5) var groupName: String?,
            @ProtoField(18) var description: String?,
            @ProtoField(19) var question: String?,
            @ProtoField(30) var announcement: String?,
        ) : ProtoMessage() {

            class Member(
                @ProtoField(2) var uid: String?,
            ) : ProtoMessage()
        }

        class CustomInfo(
            @ProtoField(3) var remark: String?,
        ) : ProtoMessage()
    }
}


