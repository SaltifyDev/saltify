package org.ntqqrev.milky.util

import org.ntqqrev.saltify.model.Gender

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