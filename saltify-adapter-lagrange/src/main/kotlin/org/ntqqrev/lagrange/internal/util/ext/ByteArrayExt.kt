package org.ntqqrev.lagrange.internal.util.ext

import org.ntqqrev.lagrange.internal.util.binary.BinaryReader
import org.ntqqrev.saltify.protobuf.ProtoBuf
import org.ntqqrev.saltify.protobuf.ProtoMessage
import java.security.MessageDigest

val EMPTY_BYTE_ARRAY = ByteArray(0)

internal fun ByteArray.calculateMD5(): ByteArray {
    val digest = MessageDigest.getInstance("MD5")
    digest.update(this)
    return digest.digest()
}

internal fun ByteArray.toHex(): String {
    return joinToString("") {
        it.toInt().and(0xff).toString(16).padStart(2, '0')
    }
}

internal inline fun <reified T : ProtoMessage> T.pb(): ByteArray = ProtoBuf.serialize(this)

internal inline fun <reified T : ProtoMessage> ByteArray.pb(): T = ProtoBuf.deserialize<T>(this)

internal fun ByteArray.writeUInt32BE(value: Long, offset: Int) {
    this[offset] = (value ushr 24).toByte()
    this[offset + 1] = (value ushr 16).toByte()
    this[offset + 2] = (value ushr 8).toByte()
    this[offset + 3] = value.toByte()
}

internal fun ByteArray.readUInt32BE(offset: Int): Long {
    return ((this[offset].toUInt() shl 24) or
            ((this[offset + 1].toUInt() and 0xffu) shl 16) or
            ((this[offset + 2].toUInt() and 0xffu) shl 8) or
            (this[offset + 3].toUInt() and 0xffu)).toLong()
}

internal fun ByteArray.reader() = BinaryReader(this)