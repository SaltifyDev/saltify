package org.ntqqrev.saltify.model

import kotlinx.datetime.Instant
import org.ntqqrev.saltify.Entity

interface Group : Entity {
    /**
     * The group ID.
     */
    val uin: Long

    /**
     * The group name.
     */
    val name: String

    /**
     * The number of members in the group.
     */
    val memberCount: Int

    /**
     * The maximum number of members allowed in the group.
     * Possible values are 200, 500, 1000, 2000 and 3000.
     */
    val maxMemberCount: Int
}