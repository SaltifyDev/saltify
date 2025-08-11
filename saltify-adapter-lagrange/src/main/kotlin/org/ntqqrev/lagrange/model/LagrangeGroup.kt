package org.ntqqrev.lagrange.model

import org.ntqqrev.lagrange.LagrangeContext
import org.ntqqrev.lagrange.internal.packet.oidb.OidbFetchGroupsResponse
import org.ntqqrev.saltify.model.Group

internal class LagrangeGroup(
    var data: OidbFetchGroupsResponse.Entry,
    override val ctx: LagrangeContext
) : Group {
    override val uin: Long get() = data.groupUin
    override val name: String get() = data.info.groupName ?: "群聊"
    val remark: String? get() = data.customInfo?.remark
    val description: String? get() = data.info.description
    val announcement: String? get() = data.info.announcement
    override val memberCount: Int get() = data.info.memberCount
    override val maxMemberCount: Int get() = data.info.memberMax
    override fun toString(): String = "${remark ?: name} ($uin)"
}


