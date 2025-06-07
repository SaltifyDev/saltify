package org.ntqqrev.lagrange.internal.service.system

import org.ntqqrev.lagrange.internal.LagrangeClient
import org.ntqqrev.lagrange.internal.packet.system.DeviceInfo
import org.ntqqrev.lagrange.internal.packet.system.RegisterInfo
import org.ntqqrev.lagrange.internal.packet.system.RegisterInfoResponse
import org.ntqqrev.lagrange.internal.service.NoInputService
import org.ntqqrev.lagrange.internal.util.ext.pb
import org.ntqqrev.lagrange.internal.util.ext.toHex

internal object BotOnline : NoInputService<Boolean> {
    override val cmd = "trpc.qq_new_tech.status_svc.StatusService.Register"

    override fun build(client: LagrangeClient, payload: Unit): ByteArray = RegisterInfo(
        guid = client.sessionStore.guid.toHex(),
        currentVersion = client.appInfo.currentVersion,
        device = DeviceInfo(
            devName = client.sessionStore.deviceName,
            devType = client.appInfo.kernel,
            osVer = "Windows 10.0.19042",
            vendorOsName = client.appInfo.vendorOs,
        )
    ).pb()

    override fun parse(client: LagrangeClient, payload: ByteArray): Boolean =
        payload.pb<RegisterInfoResponse>().message == "register success"
}