package org.ntqqrev.saltify.model

import org.ntqqrev.saltify.Entity

/**
 * Represents a user in the system.
 */
interface User : Entity {
    /**
     * The ID of the user.
     */
    val uin: Long

    /**
     * The nickname of the user.
     */
    val nickname: String

    /**
     * The gender of the user.
     */
    val gender: Gender
}