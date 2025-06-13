package org.ntqqrev.milky.util

import org.ntqqrev.saltify.model.GroupMember

internal fun String.toSaltifyRole() = when (this) {
    "owner" -> GroupMember.Role.OWNER
    "admin" -> GroupMember.Role.ADMIN
    "member" -> GroupMember.Role.MEMBER
    else -> throw IllegalArgumentException("Invalid role")
}

internal fun GroupMember.Role.toMilkyRole(): String = when (this) {
    GroupMember.Role.OWNER -> "owner"
    GroupMember.Role.ADMIN -> "admin"
    GroupMember.Role.MEMBER -> "member"
}