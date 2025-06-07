package org.ntqqrev.lagrange.internal.util.binary

internal class BinaryReader(val bytes: ByteArray) {
    private var position = 0

    val current: Int
        get() = position
    val remaining: Int
        get() = bytes.size - position

    fun readByte(): Byte {
        return bytes[position++]
    }

    fun readUByte(): UByte {
        return bytes[position++].toUByte()
    }

    fun readShort(): Short {
        return (bytes[position++].toInt() shl 8 or
                (bytes[position++].toInt() and 0xff)).toShort()
    }

    fun readUShort(): UShort {
        return (bytes[position++].toInt() shl 8 or
                (bytes[position++].toInt() and 0xff)).toUShort()
    }

    fun readInt(): Int {
        return (bytes[position++].toInt() shl 24 or
                (bytes[position++].toInt() and 0xff shl 16) or
                (bytes[position++].toInt() and 0xff shl 8) or
                (bytes[position++].toInt() and 0xff))
    }

    fun readUInt(): UInt {
        return (bytes[position++].toInt() shl 24 or
                (bytes[position++].toInt() and 0xff shl 16) or
                (bytes[position++].toInt() and 0xff shl 8) or
                (bytes[position++].toInt() and 0xff)).toUInt()
    }

    fun readLong(): Long {
        return (bytes[position++].toLong() shl 56 or
                (bytes[position++].toLong() and 0xff shl 48) or
                (bytes[position++].toLong() and 0xff shl 40) or
                (bytes[position++].toLong() and 0xff shl 32) or
                (bytes[position++].toLong() and 0xff shl 24) or
                (bytes[position++].toLong() and 0xff shl 16) or
                (bytes[position++].toLong() and 0xff shl 8) or
                (bytes[position++].toLong() and 0xff))
    }

    fun readULong(): ULong {
        return (bytes[position++].toLong() shl 56 or
                (bytes[position++].toLong() and 0xff shl 48) or
                (bytes[position++].toLong() and 0xff shl 40) or
                (bytes[position++].toLong() and 0xff shl 32) or
                (bytes[position++].toLong() and 0xff shl 24) or
                (bytes[position++].toLong() and 0xff shl 16) or
                (bytes[position++].toLong() and 0xff shl 8) or
                (bytes[position++].toLong() and 0xff)).toULong()
    }

    fun readBytes(length: Int): ByteArray {
        return bytes.copyOfRange(position, position + length).also {
            position += length
        }
    }

    fun readBytes() = readBytes(remaining)

    fun readByteArray(length: Int): ByteArray {
        return readBytes(length)
    }

    fun readByteArray() = readBytes(remaining)

    fun readString(length: Int): String {
        return String(readBytes(length))
    }

    fun skip(length: Int) {
        position += length
    }

    fun discard(length: Int) {
        position += length
    }

    private fun readLength(prefix: Prefix): UInt {
        val prefixLength = prefix.getPrefixLength()
        val includePrefix = prefix.isIncludePrefix()

        return when (prefixLength) {
            1 -> this.readByte().toUInt() - (if (includePrefix) prefixLength else 0).toUInt()
            2 -> this.readUShort().toUInt() - (if (includePrefix) prefixLength else 0).toUInt()
            4 -> this.readUInt() - (if (includePrefix) prefixLength else 0).toUInt()
            else -> 0u
        }
    }

    fun readPrefixedBytes(prefix: Prefix): ByteArray {
        val length = readLength(prefix)
        return readBytes(length.toInt())
    }

    fun readPrefixedString(prefix: Prefix): String {
        val length = readLength(prefix)
        return String(readBytes(length.toInt()))
    }
}