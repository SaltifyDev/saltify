package org.ntqqrev.lagrange.internal.util

private const val HEX = "1234567890abcdef"

internal fun generateTrace(): String {
    val sb = StringBuilder(55)

    sb.append("00") // 2 chars
    sb.append('-') // 1 char

    for (i in 0 until 32) sb.append(HEX.random()) // 32 chars
    sb.append('-') // 1 char

    for (i in 0 until 16) sb.append(HEX.random()) // 16 chars
    sb.append('-') // 1 char

    sb.append("01") // 2 chars

    return sb.toString()
}