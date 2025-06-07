package org.ntqqrev.lagrange.internal.util.ext

import io.ktor.utils.io.core.*
import kotlinx.io.*
import kotlinx.io.Buffer
import org.ntqqrev.lagrange.internal.util.binary.Prefix

internal fun Sink.writeString(value: String, prefix: Prefix = Prefix.NONE) {
    this.writeLength(value.length.toUInt(), prefix)
    this.writeText(value)
}

internal fun Sink.writeBytes(value: ByteArray, prefix: Prefix = (Prefix.NONE)) {
    this.writeLength(value.size.toUInt(), prefix)
    this.writeFully(value)
}

internal fun Sink.barrier(prefix: Prefix, addition: Int = 0, target: ((Sink).() -> Unit)) {
    val written = Buffer()
    target(written)

    writeLength(written.size.toUInt() + addition.toUInt(), prefix)
    writePacket(written.build())
}

internal fun Source.readPrefixedString(prefix: Prefix): String {
    val length = readLength(prefix)
    return readByteArray(length.toInt()).toString(Charsets.UTF_8)
}

internal fun Source.readPrefixedBytes(prefix: Prefix): ByteArray {
    val length = readLength(prefix)
    return this.readByteArray(length.toInt())
}

private fun Sink.writeLength(length: UInt, prefix: Prefix) {
    val prefixLength = prefix.getPrefixLength()
    val includePrefix = prefix.isIncludePrefix()
    val writtenLength = length + (if (includePrefix) prefixLength else 0).toUInt()

    when (prefixLength) {
        1 -> this.writeByte(writtenLength.toByte())
        2 -> this.writeUShort(writtenLength.toUShort())
        4 -> this.writeUInt(writtenLength)
        else -> {}
    }
}

private fun Source.readLength(prefix: Prefix): UInt {
    val prefixLength = prefix.getPrefixLength()
    val includePrefix = prefix.isIncludePrefix()

    return when (prefixLength) {
        1 -> this.readByte().toUInt() - (if (includePrefix) prefixLength else 0).toUInt()
        2 -> this.readUShort().toUInt() - (if (includePrefix) prefixLength else 0).toUInt()
        4 -> this.readUInt() - (if (includePrefix) prefixLength else 0).toUInt()
        else -> 0u
    }
}

fun Source.readShortLittleEndian(): Short {
    val value = this.readShort()
    return if (value.toInt() < 0) (value.toInt() + Short.MAX_VALUE * 2).toShort() else value
}

fun Source.readIntLittleEndian(): Int {
    val value = this.readInt()
    return if (value < 0) (value + Int.MAX_VALUE * 2) else value
}