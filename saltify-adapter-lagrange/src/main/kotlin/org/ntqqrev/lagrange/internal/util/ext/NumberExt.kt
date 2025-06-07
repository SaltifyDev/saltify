package org.ntqqrev.lagrange.internal.util.ext

internal fun Int.readInt32BE(): ByteArray {
    val result = ByteArray(4)
    result[0] = (this ushr 24).toByte()
    result[1] = (this ushr 16).toByte()
    result[2] = (this ushr 8).toByte()
    result[3] = this.toByte()
    return result
}

internal fun Int.readInt32LE(): IntArray {
    val result = IntArray(4)
    result[0] = this and 0xFF
    result[1] = (this ushr 8) and 0xFF
    result[2] = (this ushr 16) and 0xFF
    result[3] = (this ushr 24) and 0xFF
    return result
}