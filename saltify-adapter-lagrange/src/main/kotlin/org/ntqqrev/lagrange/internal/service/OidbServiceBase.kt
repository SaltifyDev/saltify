package org.ntqqrev.lagrange.internal.service

import org.ntqqrev.lagrange.internal.LagrangeClient
import org.ntqqrev.lagrange.internal.exception.OidbException
import org.ntqqrev.lagrange.internal.packet.oidb.OidbRequest
import org.ntqqrev.lagrange.internal.packet.oidb.OidbResponse
import org.ntqqrev.lagrange.internal.util.ext.pb

internal abstract class OidbService<T, R>(
    val oidbCmd: Int,
    val oidbSubCmd: Int
) : Service<T, R> {
    override val cmd: String = "OidbSvcTrpcTcp.0x${oidbCmd.toString(16)}_$oidbSubCmd"

    abstract fun buildOidb(client: LagrangeClient, payload: T): ByteArray
    abstract fun parseOidb(client: LagrangeClient, payload: ByteArray): R

    override fun build(client: LagrangeClient, payload: T): ByteArray =
        OidbRequest(oidbCmd, oidbSubCmd, buildOidb(client, payload)).pb()

    override fun parse(client: LagrangeClient, payload: ByteArray): R {
        val response = payload.pb<OidbResponse>()
        if (response.retCode != 0) {
            throw OidbException(oidbCmd, oidbSubCmd, response.retCode, response.errorMsg ?: "")
        }
        return parseOidb(client, response.payload!!)
    }
}

internal abstract class NoInputOidbService<R>(
    oidbCmd: Int,
    oidbSubCmd: Int
) : NoInputService<R>, OidbService<Unit, R>(oidbCmd, oidbSubCmd)

internal abstract class NoOutputOidbService<T>(
    oidbCmd: Int,
    oidbSubCmd: Int
) : OidbService<T, Unit>(oidbCmd, oidbSubCmd) {
    override fun parseOidb(client: LagrangeClient, payload: ByteArray) = Unit
}