package org.ntqqrev.lagrange.model

import kotlinx.datetime.Instant
import org.ntqqrev.lagrange.LagrangeContext
import org.ntqqrev.lagrange.internal.packet.oidb.OidbFetchGroupMembersResponse
import org.ntqqrev.saltify.model.Group
import org.ntqqrev.saltify.model.GroupMember

internal class LagrangeGroupMember(
    override val group: Group,
    var data: OidbFetchGroupMembersResponse.Entry,
    override val ctx: LagrangeContext
) : GroupMember {
    override val uin: Long get() = data.identity.uin
    override val nickname: String get() = data.memberName ?: uin.toString()
    override val gender = org.ntqqrev.saltify.model.Gender.UNKNOWN
    override val card: String? get() = data.memberCard?.memberCard
    override val specialTitle: String? get() = data.specialTitle
    override val level: Int get() = data.level?.level ?: 0
    override val joinedAt: Instant get() = Instant.fromEpochSeconds(data.joinTimestamp)
    override val lastSpokeAt: Instant? get() = Instant.fromEpochSeconds(data.lastMsgTimestamp)
    override val mutedUntil: Instant? get() = data.shutUpTimestamp?.let { Instant.fromEpochSeconds(it) }
    override val role: GroupMember.Role get() = when (data.permission) {
        0 -> GroupMember.Role.MEMBER
        1 -> GroupMember.Role.ADMIN
        2 -> GroupMember.Role.OWNER
        else -> GroupMember.Role.MEMBER
    }
    override fun toString(): String = "${card ?: nickname} ($uin)"
}


