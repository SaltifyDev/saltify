package org.ntqqrev.lagrange.internal.util.ext

internal fun String.fromHex(): ByteArray {
    val hex = this
    val result = ByteArray(hex.length / 2)
    for (i in hex.indices step 2) {
        result[i / 2] = hex.substring(i, i + 2).toInt(16).toByte()
    }
    return result
}