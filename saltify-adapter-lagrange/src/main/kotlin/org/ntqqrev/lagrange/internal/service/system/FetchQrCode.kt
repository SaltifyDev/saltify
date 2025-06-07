package org.ntqqrev.lagrange.internal.service.system

import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlinx.io.writeUInt
import kotlinx.io.writeULong
import kotlinx.io.writeUShort
import org.ntqqrev.lagrange.internal.LagrangeClient
import org.ntqqrev.lagrange.internal.packet.login.TlvQrCode
import org.ntqqrev.lagrange.internal.packet.login.TlvQrCodeD1ResponseBody
import org.ntqqrev.lagrange.internal.service.NoInputService
import org.ntqqrev.lagrange.internal.util.binary.Prefix
import org.ntqqrev.lagrange.internal.util.ext.pb
import org.ntqqrev.lagrange.internal.util.ext.reader
import org.ntqqrev.lagrange.internal.util.ext.writeBytes

internal object FetchQrCode : NoInputService<FetchQrCode.Result> {
    override val cmd = "wtlogin.trans_emp"

    override fun build(client: LagrangeClient, payload: Unit): ByteArray {
        val tlvPack = TlvQrCode(client).apply {
            tlv16()
            tlv1b()
            tlv1d()
            tlv33()
            tlv35()
            tlv66()
            tlvD1()
        }
        val packet = Buffer().apply {
            writeUShort(0u)
            writeUInt(client.appInfo.appId.toUInt())
            writeULong(0u) // uin
            writeBytes(ByteArray(0))
            writeByte(0)
            writeBytes(ByteArray(0), Prefix.UINT_16 or Prefix.LENGTH_ONLY)
            writeBytes(tlvPack.build())
        }
        return client.loginLogic.buildCode2DPacket(packet.readByteArray(), 0x31u)
    }

    override fun parse(client: LagrangeClient, payload: ByteArray): Result {
        val wtlogin = client.loginLogic.parseWtLogin(payload)
        val code2d = client.loginLogic.parseCode2DPacket(wtlogin)
        val reader = code2d.reader()
        reader.discard(1)
        val sig = reader.readPrefixedBytes(Prefix.UINT_16 or Prefix.LENGTH_ONLY)
        val tlv = client.loginLogic.readTlv(reader)
        client.sessionStore.qrSig = sig
        val respD1Body = tlv.getValue(0xD1u).pb<TlvQrCodeD1ResponseBody>()
        return Result(
            qrCodeUrl = respD1Body.qrCodeUrl,
            qrCodePng = tlv.getValue(0x17u)
        )
    }

    class Result(
        val qrCodeUrl: String,
        val qrCodePng: ByteArray
    )
}