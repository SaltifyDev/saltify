package org.ntqqrev.lagrange.internal.packet.login

import io.ktor.utils.io.core.*
import kotlinx.io.*
import kotlinx.io.Buffer
import org.ntqqrev.lagrange.internal.LagrangeClient
import org.ntqqrev.lagrange.internal.util.binary.*
import org.ntqqrev.lagrange.internal.util.ext.*
import org.ntqqrev.saltify.protobuf.ProtoMessage
import org.ntqqrev.saltify.protobuf.annotation.ProtoField

internal class TlvQrCode(val client: LagrangeClient) {
    private val builder = Buffer()

    private var tlvCount: UShort = 0u

    fun tlv16() = defineTlv(0x16u) {
        writeUInt(0u)
        writeInt(client.appInfo.appId)
        writeInt(client.appInfo.subAppId)
        writeFully(client.sessionStore.guid)
        writeString(client.appInfo.packageName, Prefix.UINT_16 or Prefix.LENGTH_ONLY)
        writeString(client.appInfo.ptVersion, Prefix.UINT_16 or Prefix.LENGTH_ONLY)
        writeString(client.appInfo.packageName, Prefix.UINT_16 or Prefix.LENGTH_ONLY)
    }

    fun tlv1b() = defineTlv(0x1bu) {
        writeUInt(0u) // micro
        writeUInt(0u) // version
        writeUInt(3u) // size
        writeUInt(4u) // margin
        writeUInt(72u) // dpi
        writeUInt(2u) // eclevel
        writeUInt(2u) // hint
        writeUShort(0u) // unknown
    }

    fun tlv1d() = defineTlv(0x1du) {
        writeUByte(1u)
        writeInt(client.appInfo.mainSigMap) // misc bitmap
        writeUInt(0u)
        writeUByte(0u)
    }

    fun tlv33() = defineTlv(0x33u) {
        writeFully(client.sessionStore.guid)
    }

    fun tlv35() = defineTlv(0x35u) {
        writeInt(client.appInfo.ssoVersion)
    }

    fun tlv66() = defineTlv(0x66u) {
        writeInt(client.appInfo.ssoVersion)
    }

    fun tlvD1() = defineTlv(0xd1u) {
        writeFully(
            TlvQrCodeD1Body(
                system = TlvQrCodeD1Body.System(
                    os = client.appInfo.os,
                    deviceName = client.sessionStore.deviceName,
                ),
                typeBuf = "3001".fromHex()
            ).pb()
        )
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

class TlvQrCodeD1Body(
    @ProtoField(1) var system: System,
    @ProtoField(4) var typeBuf: ByteArray,
) : ProtoMessage() {
    class System(
        @ProtoField(1) var os: String,
        @ProtoField(2) var deviceName: String,
    ) : ProtoMessage()
}

class TlvQrCodeD1ResponseBody(
    @ProtoField(2) var qrCodeUrl: String,
    @ProtoField(3) var qrSig: String,
) : ProtoMessage()