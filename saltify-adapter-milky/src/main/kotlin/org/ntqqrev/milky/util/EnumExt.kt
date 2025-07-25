package org.ntqqrev.milky.util

import org.ntqqrev.saltify.event.RequestState
import org.ntqqrev.saltify.message.ImageSubType
import org.ntqqrev.saltify.message.MessageScene
import org.ntqqrev.saltify.model.Gender
import org.ntqqrev.saltify.model.GroupMember

internal fun String.toSaltifyMessageScene() = when (this) {
    "friend" -> MessageScene.FRIEND
    "group" -> MessageScene.GROUP
    "temp" -> MessageScene.TEMP
    else -> throw IllegalArgumentException("Invalid message scene")
}

internal fun MessageScene.toMilkyMessageScene(): String = when (this) {
    MessageScene.FRIEND -> "friend"
    MessageScene.GROUP -> "group"
    MessageScene.TEMP -> "temp"
}

internal fun String.toSaltifyGender() = when (this) {
    "male" -> Gender.MALE
    "female" -> Gender.FEMALE
    "unknown" -> Gender.UNKNOWN
    else -> throw IllegalArgumentException("Invalid sex")
}

internal fun Gender.toMilkySex(): String = when (this) {
    Gender.MALE -> "male"
    Gender.FEMALE -> "female"
    Gender.UNKNOWN -> "unknown"
}

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

internal fun String.toImageSubType() = when (this) {
    "normal" -> ImageSubType.NORMAL
    "sticker" -> ImageSubType.STICKER
    else -> throw IllegalArgumentException("Invalid image sub type")
}

internal fun ImageSubType.toMilkySubType(): String = when (this) {
    ImageSubType.NORMAL -> "normal"
    ImageSubType.STICKER -> "sticker"
}

internal fun String.toSaltifyRequestState() = when (this) {
    "pending" -> RequestState.PENDING
    "accepted" -> RequestState.ACCEPTED
    "rejected" -> RequestState.REJECTED
    "ignored" -> RequestState.IGNORED
    else -> throw IllegalArgumentException("Invalid request state")
}

internal fun RequestState.toMilkyState(): String = when (this) {
    RequestState.PENDING -> "pending"
    RequestState.ACCEPTED -> "accepted"
    RequestState.REJECTED -> "rejected"
    RequestState.IGNORED -> "ignored"
}