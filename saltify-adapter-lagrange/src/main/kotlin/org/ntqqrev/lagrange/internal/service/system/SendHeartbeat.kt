package org.ntqqrev.lagrange.internal.service.system

import org.ntqqrev.lagrange.internal.LagrangeClient
import org.ntqqrev.lagrange.internal.packet.system.SsoHeartbeat
import org.ntqqrev.lagrange.internal.service.NoInputService
import org.ntqqrev.lagrange.internal.util.ext.pb

internal object SendHeartbeat : NoInputService<Unit> {
    override val cmd = "trpc.qq_new_tech.status_svc.StatusService.SsoHeartBeat"

    private val heartbeatPb = SsoHeartbeat().pb()

    override fun build(client: LagrangeClient, payload: Unit) = heartbeatPb

    override fun parse(client: LagrangeClient, payload: ByteArray) = Unit
}