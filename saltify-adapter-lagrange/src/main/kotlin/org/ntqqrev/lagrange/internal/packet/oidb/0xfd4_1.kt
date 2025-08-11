package org.ntqqrev.lagrange.internal.packet.oidb

import org.ntqqrev.saltify.protobuf.ProtoMessage
import org.ntqqrev.saltify.protobuf.annotation.ProtoField

class OidbFetchFriendsRequest(
    @ProtoField(2) var friendCount: Int = 300,
    @ProtoField(5) var nextUin: UinBody,
    @ProtoField(10001) var body: Map<Int, NumberList> = mapOf(
        1 to NumberList(
            numbers = listOf(
                QueryKey.SIGNATURE.value,
                QueryKey.REMARK.value,
                QueryKey.NICKNAME.value,
                QueryKey.QID.value,
            )
        ),
        4 to NumberList(
            numbers = listOf(
                QueryKey.UNKNOWN_100.value,
                QueryKey.UNKNOWN_101.value,
                QueryKey.SIGNATURE.value,
            )
        )
    ),
) : ProtoMessage() {
    class NumberList(
        @ProtoField(1) var numbers: List<Int>,
    ) : ProtoMessage()
}

class OidbFetchFriendsResponse(
    @ProtoField(2) var next: UinBody?,
    @ProtoField(3) var displayFriendCount: Long,
    @ProtoField(6) var timestamp: Long,
    @ProtoField(7) var selfUin: Long,
    @ProtoField(101) var friends: List<Entry>,
    @ProtoField(102) var categories: List<Category>,
) : ProtoMessage() {
    class Entry(
        @ProtoField(1) var uid: String,
        @ProtoField(2) var category: Int?,
        @ProtoField(3) var uin: Long,
        @ProtoField(10001) var additional: Map<Int, Properties>,
    ) : ProtoMessage() {
        class Properties(
            @ProtoField(2) var properties: Map<Int, String>,
        ) : ProtoMessage()
    }

    class Category(
        @ProtoField(2) var name: String,
        @ProtoField(3) var id: Int,
    ) : ProtoMessage()
}

class UinBody(
    @ProtoField(1) var uin: Long?,
) : ProtoMessage()

@JvmInline
value class QueryKey(val value: Int) {
    companion object {
        val UNKNOWN_100 = QueryKey(100)
        val UNKNOWN_101 = QueryKey(101)
        val SIGNATURE = QueryKey(102)
        val REMARK = QueryKey(103)
        val NICKNAME = QueryKey(20002)
        val QID = QueryKey(27394)
    }
}


