package org.ntqqrev.lagrange.internal.service.system

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.utils.io.core.writeFully
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlinx.io.writeUShort
import org.ntqqrev.lagrange.internal.LagrangeClient
import org.ntqqrev.lagrange.internal.packet.login.Tlv
import org.ntqqrev.lagrange.internal.packet.login.Tlv543Body
import org.ntqqrev.lagrange.internal.service.NoInputService
import org.ntqqrev.lagrange.internal.util.binary.Prefix
import org.ntqqrev.lagrange.internal.util.crypto.TEA
import org.ntqqrev.lagrange.internal.util.ext.pb
import org.ntqqrev.lagrange.internal.util.ext.reader

internal object WtLogin : NoInputService<Boolean> {
    private val logger = KotlinLogging.logger { }

    override val cmd = "wtlogin.login"

    override fun build(client: LagrangeClient, payload: Unit): ByteArray {
        val tlvPack = Tlv(client).apply {
            tlv106A2()
            tlv144()
            tlv116()
            tlv142()
            tlv145()
            tlv18()
            tlv141()
            tlv177()
            tlv191()
            tlv100()
            tlv107()
            tlv318()
            tlv16a()
            tlv166()
            tlv521()
        }
        val packet = Buffer().apply {
            writeUShort(9u) // internal command
            writeFully(tlvPack.build())
        }
        return client.loginLogic.buildWtLogin(packet.readByteArray(), 2064u)
    }

    override fun parse(
        client: LagrangeClient,
        payload: ByteArray
    ): Boolean {
        val reader = client.loginLogic.parseWtLogin(payload).reader()

        val command = reader.readUShort()
        val state = reader.readUByte()
        val tlv119Reader = client.loginLogic.readTlv(reader)

        if (state.toInt() == 0) {
            val tlv119 = tlv119Reader[0x119u]!!
            val array = TEA.decrypt(tlv119, client.sessionStore.tgtgt)
            val tlvPack = client.loginLogic.readTlv(array.reader())
            client.sessionStore.apply {
                d2Key = tlvPack[0x305u]!!
                uid = tlvPack[0x543u]!!.pb<Tlv543Body>().layer1.layer2.uid
                a2 = tlvPack[0x10Au]!!
                d2 = tlvPack[0x143u]!!
                encryptedA1 = tlvPack[0x106u]!!
            }
            return true
        } else {
            val tlv146 = tlv119Reader[0x146u]!!.reader()
            val state = tlv146.readUInt()
            val tag = tlv146.readPrefixedString(Prefix.UINT_16 or Prefix.LENGTH_ONLY)
            val message = tlv146.readPrefixedString(Prefix.UINT_16 or Prefix.LENGTH_ONLY)
            logger.error { "WtLogin failed: state=$state, tag=$tag, message=$message" }
            return false
        }
    }
}