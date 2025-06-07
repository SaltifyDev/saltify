package org.ntqqrev.lagrange.internal.logic

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.writeFully
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import org.ntqqrev.lagrange.common.SignProvider
import org.ntqqrev.lagrange.internal.LagrangeClient
import org.ntqqrev.lagrange.internal.packet.SsoResponse
import org.ntqqrev.lagrange.internal.packet.system.SsoReservedFields
import org.ntqqrev.lagrange.internal.service.system.SendHeartbeat
import org.ntqqrev.lagrange.internal.util.binary.*
import org.ntqqrev.lagrange.internal.util.crypto.TEA
import org.ntqqrev.lagrange.internal.util.ext.*
import org.ntqqrev.lagrange.internal.util.generateTrace
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.InflaterInputStream
import kotlin.io.use
import kotlin.random.Random

internal class PacketLogic(client: LagrangeClient) : AbstractLogic(client) {
    private var sequence = Random.nextInt(0x10000, 0x20000)
    private val host = "msfwifi.3g.qq.com"
    private val port = 8080

    private val selectorManager = ActorSelectorManager(client.scope.coroutineContext)
    private val socket = aSocket(selectorManager).tcp()
    private lateinit var input: ByteReadChannel
    private lateinit var output: ByteWriteChannel
    private val pending = ConcurrentHashMap<Int, CompletableDeferred<SsoResponse>>()
    private val headerLength = 4
    var connected = false

    val signRequiredCommand = setOf(
        "trpc.o3.ecdh_access.EcdhAccess.SsoEstablishShareKey",
        "trpc.o3.ecdh_access.EcdhAccess.SsoSecureAccess",
        "trpc.o3.report.Report.SsoReport",
        "MessageSvc.PbSendMsg",
        "wtlogin.trans_emp",
        "wtlogin.login",
        "trpc.login.ecdh.EcdhService.SsoKeyExchange",
        "trpc.login.ecdh.EcdhService.SsoNTLoginPasswordLogin",
        "trpc.login.ecdh.EcdhService.SsoNTLoginEasyLogin",
        "trpc.login.ecdh.EcdhService.SsoNTLoginPasswordLoginNewDevice",
        "trpc.login.ecdh.EcdhService.SsoNTLoginEasyLoginUnusualDevice",
        "trpc.login.ecdh.EcdhService.SsoNTLoginPasswordLoginUnusualDevice",
        "OidbSvcTrpcTcp.0x11ec_1",
        "OidbSvcTrpcTcp.0x758_1", // create a group
        "OidbSvcTrpcTcp.0x7c1_1",
        "OidbSvcTrpcTcp.0x7c2_5", // request friend
        "OidbSvcTrpcTcp.0x10db_1",
        "OidbSvcTrpcTcp.0x8a1_7", // request group
        "OidbSvcTrpcTcp.0x89a_0",
        "OidbSvcTrpcTcp.0x89a_15",
        "OidbSvcTrpcTcp.0x88d_0", // fetch group detail
        "OidbSvcTrpcTcp.0x88d_14",
        "OidbSvcTrpcTcp.0x112a_1",
        "OidbSvcTrpcTcp.0x587_74",
        "OidbSvcTrpcTcp.0x1100_1",
        "OidbSvcTrpcTcp.0x1102_1",
        "OidbSvcTrpcTcp.0x1103_1",
        "OidbSvcTrpcTcp.0x1107_1",
        "OidbSvcTrpcTcp.0x1105_1",
        "OidbSvcTrpcTcp.0xf88_1",
        "OidbSvcTrpcTcp.0xf89_1",
        "OidbSvcTrpcTcp.0xf57_1",
        "OidbSvcTrpcTcp.0xf57_106",
        "OidbSvcTrpcTcp.0xf57_9",
        "OidbSvcTrpcTcp.0xf55_1",
        "OidbSvcTrpcTcp.0xf67_1",
        "OidbSvcTrpcTcp.0xf67_5",
        "OidbSvcTrpcTcp.0x6d9_4"
    )

    private val logger = KotlinLogging.logger { }

    suspend fun connect() {
        val s = socket.connect(host, port)
        input = s.openReadChannel()
        output = s.openWriteChannel(autoFlush = true)
        logger.info { "Connected to $host:$port" }
        connected = true

        client.scope.launch {
            handleReceiveLoop()
        }

        client.scope.launch {
            while (connected) {
                client.callService(SendHeartbeat)
                delay(300_000) // 5 minutes
            }
        }
    }

    suspend fun disconnect() {
        input.cancel()
        output.flushAndClose()
        connected = false
    }

    suspend fun sendPacket(cmd: String, payload: ByteArray): SsoResponse {
        val sequence = this.sequence++
        val sso = buildSso(cmd, payload, sequence)
        val service = buildService(sso)

        val deferred = CompletableDeferred<SsoResponse>()
        pending[sequence] = deferred

        output.writePacket(service)
        logger.trace { "[seq=$sequence] -> $cmd" }

        return deferred.await()
    }

    private suspend fun handleReceiveLoop() {
        while (connected) {
            try {
                val header = input.readByteArray(headerLength)
                val packetLength = header.readUInt32BE(0)
                val packet = input.readByteArray(packetLength.toInt() - 4)
                val service = parseService(packet)
                val sso = parseSso(service)
                logger.trace { "[seq=${sso.sequence}] <- ${sso.command} (code=${sso.retCode})" }
                pending.remove(sso.sequence).also {
                    if (it != null) {
                        it.complete(sso)
                    } else {
                        // TODO: client.eventContext.process(sso)
                    }
                }
            } catch (e: Exception) {
                logger.error(e) { "Error receiving packet" }
                disconnect()
            }
        }
    }

    private fun buildService(sso: ByteArray): Buffer {
        val packet = Buffer()

        packet.barrier(Prefix.UINT_32 or Prefix.INCLUDE_PREFIX) {
            writeInt(12)
            writeByte(if (client.sessionStore.d2.isEmpty()) 2 else 1)
            writeBytes(client.sessionStore.d2, Prefix.UINT_32 or Prefix.INCLUDE_PREFIX)
            writeByte(0) // unknown
            writeString(client.sessionStore.uin.toString(), Prefix.UINT_32 or Prefix.INCLUDE_PREFIX)
            writeBytes(TEA.encrypt(sso, client.sessionStore.d2Key))
        }

        return packet
    }

    val buildSsoFixedBytes = byteArrayOf(
        0x02, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00,
    )

    private suspend fun buildSso(command: String, payload: ByteArray, sequence: Int): ByteArray {
        val packet = Buffer()
        val ssoReserved = buildSsoReserved(command, payload, sequence)

        packet.barrier(Prefix.UINT_32 or Prefix.INCLUDE_PREFIX) {
            writeInt(sequence)
            writeInt(client.appInfo.subAppId)
            writeInt(2052)  // locale id
            writeFully(buildSsoFixedBytes)
            writeBytes(client.sessionStore.a2, Prefix.UINT_32 or Prefix.INCLUDE_PREFIX)
            writeString(command, Prefix.UINT_32 or Prefix.INCLUDE_PREFIX)
            writeBytes(ByteArray(0), Prefix.UINT_32 or Prefix.INCLUDE_PREFIX) // unknown
            writeString(client.sessionStore.guid.toHex(), Prefix.UINT_32 or Prefix.INCLUDE_PREFIX)
            writeBytes(ByteArray(0), Prefix.UINT_32 or Prefix.INCLUDE_PREFIX) // unknown
            writeString(client.appInfo.currentVersion, Prefix.UINT_16 or Prefix.INCLUDE_PREFIX)
            writeBytes(ssoReserved, Prefix.UINT_32 or Prefix.INCLUDE_PREFIX)
        }

        packet.writeBytes(payload, Prefix.UINT_32 or Prefix.INCLUDE_PREFIX)

        return packet.readByteArray()
    }

    private suspend fun buildSsoReserved(command: String, payload: ByteArray, sequence: Int): ByteArray {
        var result: SignProvider.Result? = null

        if (signRequiredCommand.contains(command)) {
            result = client.signProvider.sign(command, sequence, payload)
        }

        return SsoReservedFields(
            trace = generateTrace(),
            uid = client.sessionStore.uid,
            secureInfo = if (result != null) SsoReservedFields.SecureInfo(
                sign = result.sign,
                token = result.token,
                extra = result.extra
            ) else null
        ).pb()
    }

    private fun parseSso(packet: ByteArray): SsoResponse {
        val reader = packet.reader()
        /* val headLen = */ reader.readUInt()
        val sequence = reader.readUInt()
        val retCode = reader.readInt()
        val extra = reader.readPrefixedString(Prefix.UINT_32 or Prefix.INCLUDE_PREFIX)
        val command = reader.readPrefixedString(Prefix.UINT_32 or Prefix.INCLUDE_PREFIX)
        /* val msgCookie = */ reader.readPrefixedBytes(Prefix.UINT_32 or Prefix.INCLUDE_PREFIX)
        val isCompressed = reader.readInt() == 1
        /* val reserveField = */ reader.readPrefixedBytes(Prefix.UINT_32)
        var payload = reader.readPrefixedBytes(Prefix.UINT_32 or Prefix.INCLUDE_PREFIX)

        if (isCompressed) {
            InflaterInputStream(payload.inputStream()).use {
                payload = it.readBytes()
            }
        }

        return if (retCode == 0) {
            SsoResponse(retCode, command, payload, sequence.toInt())
        } else {
            SsoResponse(retCode, command, payload, sequence.toInt(), extra)
        }
    }

    private fun parseService(raw: ByteArray): ByteArray {
        val reader = raw.reader()

        val protocol = reader.readUInt()
        val authFlag = reader.readByte()
        /* val flag = */ reader.readByte()
        /* val uin = */ reader.readPrefixedString(Prefix.UINT_32 or Prefix.INCLUDE_PREFIX)

        if (protocol != 12u && protocol != 13u) throw Exception("Unrecognized protocol: $protocol")

        val encrypted = reader.readByteArray()
        return when (authFlag) {
            0.toByte() -> encrypted
            1.toByte() -> TEA.decrypt(encrypted, client.sessionStore.d2Key)
            2.toByte() -> TEA.decrypt(encrypted, ByteArray(16))
            else -> throw Exception("Unrecognized auth flag: $authFlag")
        }
    }
}

