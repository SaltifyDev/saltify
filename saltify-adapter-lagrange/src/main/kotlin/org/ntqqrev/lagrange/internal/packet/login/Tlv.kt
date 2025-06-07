package org.ntqqrev.lagrange.internal.packet.login

import io.ktor.utils.io.core.*
import kotlinx.io.*
import kotlinx.io.Buffer
import org.ntqqrev.lagrange.internal.LagrangeClient
import org.ntqqrev.lagrange.internal.util.binary.Prefix
import org.ntqqrev.lagrange.internal.util.crypto.TEA
import org.ntqqrev.lagrange.internal.util.ext.barrier
import org.ntqqrev.lagrange.internal.util.ext.writeBytes
import org.ntqqrev.lagrange.internal.util.ext.writeString
import org.ntqqrev.saltify.protobuf.ProtoMessage
import org.ntqqrev.saltify.protobuf.annotation.ProtoField
import kotlin.random.Random

internal class Tlv(val client: LagrangeClient) {
    private val builder = Buffer()

    private var tlvCount: UShort = 0u

    fun tlv18() = defineTlv(0x18u) {
        writeUShort(0u) // ping ver
        writeUInt(5u)
        writeUInt(0u)
        writeUInt(8001u) // app client ver
        writeUInt(client.sessionStore.uin.toUInt())
        writeUShort(0u)
        writeUShort(0u)
    }

    fun tlv100() = defineTlv(0x100u) {
        writeUShort(0u) // db buf ver
        writeUInt(5u) // sso ver, dont over 7
        writeInt(client.appInfo.appId)
        writeInt(client.appInfo.subAppId)
        writeInt(client.appInfo.appClientVersion) // app client ver
        writeInt(client.appInfo.mainSigMap)
    }

    fun tlv106A2() = defineTlv(0x106u) {
        writeFully(client.sessionStore.encryptedA1)
    }

    fun tlv106(md5pass: ByteArray) = defineTlv(0x106u) {
        val body = Buffer().apply {
            writeUShort(4u) // tgtgt ver
            writeFully(Random.nextBytes(4)) // crypto.randomBytes(4)
            writeUInt(0u) // sso ver
            writeInt(client.appInfo.appId)
            writeInt(8001) // app client ver
            writeULong(client.sessionStore.uin.toULong())
            writeInt((System.currentTimeMillis() / 1000).toInt())
            writeUInt(0u) // dummy ip
            writeByte(1) // save password
            writeFully(md5pass)
            writeFully(client.sessionStore.a2)
            writeUInt(0u)
            writeByte(1) // guid available
            writeFully(client.sessionStore.guid)
            writeUInt(1u)
            writeUInt(1u) // login type password
            writeString(client.sessionStore.uin.toString(), Prefix.UINT_16 or Prefix.LENGTH_ONLY)
        }

        val buf = Buffer()

        buf.writeInt(client.sessionStore.uin.toInt())
        buf.writeFully(ByteArray(4))
        buf.writeFully(md5pass)

        writeBytes(TEA.encrypt(body.readByteArray(), buf.readByteArray()))
    }

    fun tlv107() = defineTlv(0x107u) {
        writeUShort(1u) // pic type
        writeUByte(0x0du) // captcha type
        writeUShort(0u) // pic size
        writeUByte(1u) // ret type
    }

    fun tlv116() = defineTlv(0x116u) {
        writeUByte(0u)
        writeUInt(12058620u)
        writeInt(client.appInfo.subSigMap)
        writeUByte(0u)
    }

    fun tlv124() = defineTlv(0x124u) {
        writeBytes(ByteArray(12))
    }

    fun tlv128() = defineTlv(0x128u) {
        writeUShort(0u)
        writeUByte(0u) // guid new
        writeUByte(0u) // guid available
        writeUByte(0u) // guid changed
        writeUInt(0u) // guid flag
        writeString(client.appInfo.os, Prefix.UINT_16 or Prefix.LENGTH_ONLY)
        writeBytes(client.sessionStore.guid, Prefix.UINT_16 or Prefix.LENGTH_ONLY)
        writeString("", Prefix.UINT_16 or Prefix.LENGTH_ONLY) // brand
    }

    fun tlv141() = defineTlv(0x141u) {
        writeString("Unknown", Prefix.UINT_32 or Prefix.LENGTH_ONLY)
        writeUInt(0u)
    }

    fun tlv142() = defineTlv(0x142u) {
        writeUShort(0u)
        writeString(client.appInfo.packageName, Prefix.UINT_16 or Prefix.LENGTH_ONLY)
    }

    fun tlv144() = defineTlv(0x144u) {
        val tlvPack = Tlv(client).apply {
            tlv16e()
            tlv147()
            tlv128()
            tlv124()
        }

        writeBytes(TEA.encrypt(tlvPack.build(), client.sessionStore.tgtgt))
    }

    fun tlv145() = defineTlv(0x145u) {
        writeBytes(client.sessionStore.guid)
    }

    fun tlv147() = defineTlv(0x147u) {
        writeInt(client.appInfo.appId)
        writeString(client.appInfo.ptVersion, Prefix.UINT_16 or Prefix.LENGTH_ONLY)
        writeString(client.appInfo.packageName, Prefix.UINT_16 or Prefix.LENGTH_ONLY)
    }

    fun tlv166() = defineTlv(0x166u) {
        writeUByte(5u)
    }

    fun tlv16a() = defineTlv(0x16au) {
        writeFully(client.sessionStore.noPicSig)
    }

    fun tlv16e() = defineTlv(0x16eu) {
        writeString(client.sessionStore.deviceName)
    }

    fun tlv177() = defineTlv(0x177u) {
        writeUByte(1u)
        writeUInt(0u)
        writeString(client.appInfo.wtLoginSdk, Prefix.UINT_16 or Prefix.LENGTH_ONLY)
    }

    fun tlv191() = defineTlv(0x191u) {
        writeUByte(0u)
    }

    fun tlv318() = defineTlv(0x318u) {

    }

    fun tlv521() = defineTlv(0x521u) {
        writeUInt(0x13u) // product type
        writeString("basicim", Prefix.UINT_16 or Prefix.LENGTH_ONLY)
    }

    fun build(): ByteArray = Buffer().apply {
        writeUShort(tlvCount)
        writeFully(builder.readByteArray())
    }.readByteArray()

    private fun defineTlv(tag: UShort, tlv: Sink.() -> Unit) {
        tlvCount++

        builder.writeUShort(tag)
        builder.barrier(Prefix.UINT_16 or Prefix.LENGTH_ONLY) {
            tlv()
        }
    }
}

class Tlv543Body(
    @ProtoField(9) var layer1: Layer1,
) : ProtoMessage() {
    class Layer1(
        @ProtoField(11) var layer2: Layer2,
    ) : ProtoMessage() {
        class Layer2(
            @ProtoField(1) var uid: String,
        ) : ProtoMessage()
    }
}