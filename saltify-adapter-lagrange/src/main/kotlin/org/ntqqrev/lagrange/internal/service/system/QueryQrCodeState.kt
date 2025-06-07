package org.ntqqrev.lagrange.internal.service.system

import kotlinx.io.*
import org.ntqqrev.lagrange.internal.LagrangeClient
import org.ntqqrev.lagrange.internal.service.NoInputService
import org.ntqqrev.lagrange.internal.util.binary.Prefix
import org.ntqqrev.lagrange.internal.util.ext.reader
import org.ntqqrev.lagrange.internal.util.ext.writeBytes

internal object QueryQrCodeState : NoInputService<QueryQrCodeState.Result> {
    override val cmd = "wtlogin.trans_emp"

    override fun build(client: LagrangeClient, payload: Unit): ByteArray {
        val packet = Buffer().apply {
            writeUShort(0u)
            writeUInt(client.appInfo.appId.toUInt())
            writeBytes(client.sessionStore.qrSig, Prefix.UINT_16 or Prefix.LENGTH_ONLY)
            writeULong(0u) // uin
            writeByte(0)
            writeBytes(ByteArray(0), Prefix.UINT_16 or Prefix.LENGTH_ONLY)
            writeUShort(0u)  // actually it is the tlv count, but there is no tlv so 0x0 is used
        }
        return client.loginLogic.buildCode2DPacket(packet.readByteArray(), 0x12u)
    }

    override fun parse(
        client: LagrangeClient,
        payload: ByteArray
    ): Result {
        val wtlogin = client.loginLogic.parseWtLogin(payload)
        val reader = client.loginLogic.parseCode2DPacket(wtlogin).reader()
        val retCode = Result(reader.readByte())
        if (retCode.value == Result.Confirmed.value) {
            reader.discard(4)
            client.sessionStore.uin = reader.readUInt().toLong()
            reader.discard(4)

            val tlv = client.loginLogic.readTlv(reader)
            client.sessionStore.tgtgt = tlv[0x1eu]!!
            client.sessionStore.encryptedA1 = tlv[0x18u]!!
            client.sessionStore.noPicSig = tlv[0x19u]!!
        }
        return retCode
    }

    open class Result(val value: Byte) {
        data object Unknown : Result(-1)
        data object Confirmed : Result(0)
        data object CodeExpired : Result(17)
        data object WaitingForScan : Result(48)
        data object WaitingForConfirm : Result(53)
        data object Canceled : Result(54)

        companion object {
            fun values(): Array<Result> =
                arrayOf(Unknown, Confirmed, CodeExpired, WaitingForScan, WaitingForConfirm, Canceled)

            fun valueOf(value: String): Result = when (value) {
                "Unknown" -> Unknown
                "Confirmed" -> Confirmed
                "CodeExpired" -> CodeExpired
                "WaitingForScan" -> WaitingForScan
                "WaitingForConfirm" -> WaitingForConfirm
                "Canceled" -> Canceled
                else -> throw IllegalArgumentException("No object org.lagrange.dev.packet.login.QRCodeState.$value")
            }

            fun getString(value: Result): String = when (value.value) {
                Unknown.value -> "Unknown"
                Confirmed.value -> "Confirmed"
                CodeExpired.value -> "CodeExpired"
                WaitingForScan.value -> "WaitingForScan"
                WaitingForConfirm.value -> "WaitingForConfirm"
                Canceled.value -> "Canceled"
                else -> throw IllegalArgumentException("No object org.lagrange.dev.packet.login.QRCodeState.$value")
            }
        }
    }
}