package org.ntqqrev.lagrange.internal.packet.oidb


import org.ntqqrev.saltify.protobuf.ProtoMessage
import org.ntqqrev.saltify.protobuf.annotation.ProtoField


class OidbFetchGroupMembersRequest(
    @ProtoField(1) var groupUin: Long,
    @ProtoField(2) var field2: Int = 5,
    @ProtoField(3) var field3: Int = 2,
    @ProtoField(4) var body: Body = Body(),
    @ProtoField(15) var token: String?,
) : ProtoMessage() {
    class Body(
        @ProtoField(10) var memberName: Boolean = true,
        @ProtoField(11) var memberCard: Boolean = true,
        @ProtoField(12) var level: Boolean = true,
        @ProtoField(13) var field13: Boolean = true,
        @ProtoField(16) var field16: Boolean = true,
        @ProtoField(17) var specialTitle: Boolean = true,
        @ProtoField(18) var field18: Boolean = true,
        @ProtoField(20) var field20: Boolean = true,
        @ProtoField(21) var field21: Boolean = true,
        @ProtoField(100) var joinTimestamp: Boolean = true,
        @ProtoField(101) var lastMsgTimestamp: Boolean = true,
        @ProtoField(102) var shutUpTimestamp: Boolean = true,
        @ProtoField(103) var field103: Boolean = true,
        @ProtoField(104) var field104: Boolean = true,
        @ProtoField(105) var field105: Boolean = true,
        @ProtoField(106) var field106: Boolean = true,
        @ProtoField(107) var permission: Boolean = true,
        @ProtoField(200) var field200: Boolean = true,
        @ProtoField(201) var field201: Boolean = true,
    ) : ProtoMessage()
}


class OidbFetchGroupMembersResponse(
    @ProtoField(1) var groupUin: Long,
    @ProtoField(2) var entries: List<Entry>,
    @ProtoField(3) var field3: Long,
    @ProtoField(5) var memberChangeSeq: Long,
    @ProtoField(6) var memberCardChangeSeq: Long,
    @ProtoField(15) var token: String?,
) : ProtoMessage() {
    class Entry(
        @ProtoField(1) var identity: Identity,
        @ProtoField(10) var memberName: String?,
        @ProtoField(17) var specialTitle: String?,
        @ProtoField(11) var memberCard: Card?,
        @ProtoField(12) var level: Level?,
        @ProtoField(100) var joinTimestamp: Long,
        @ProtoField(101) var lastMsgTimestamp: Long,
        @ProtoField(102) var shutUpTimestamp: Long?,
        @ProtoField(107) var permission: Int = 0,
    ) : ProtoMessage() {

        class Identity(
            @ProtoField(2) var uid: String,
            @ProtoField(4) var uin: Long,
        ) : ProtoMessage()

        class Card(
            @ProtoField(2) var memberCard: String?,
        ) : ProtoMessage()

        class Level(
            @ProtoField(1) var infos: List<Int>,
            @ProtoField(2) var level: Int,
        ) : ProtoMessage()
    }
}


