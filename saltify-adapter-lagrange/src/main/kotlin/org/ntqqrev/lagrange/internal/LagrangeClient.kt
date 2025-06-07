package org.ntqqrev.lagrange.internal

import kotlinx.coroutines.CoroutineScope
import org.ntqqrev.lagrange.common.*
import org.ntqqrev.lagrange.internal.exception.ServiceException
import org.ntqqrev.lagrange.internal.logic.*
import org.ntqqrev.lagrange.internal.service.NoInputService
import org.ntqqrev.lagrange.internal.service.Service

internal class LagrangeClient(
    val appInfo: AppInfo,
    val sessionStore: SessionStore,
    val signProvider: SignProvider,
    val scope: CoroutineScope
) {
    val packetLogic = PacketLogic(this)
    val loginLogic = LoginLogic(this)

    suspend fun <T, R> callService(service: Service<T, R>, payload: T): R {
        val byteArray = service.build(this, payload)
        val resp = packetLogic.sendPacket(service.cmd, byteArray)
        if (resp.retCode != 0) {
            throw ServiceException(
                service.cmd,
                resp.retCode,
                resp.extra ?: ""
            )
        }
        return service.parse(this, resp.response)
    }

    suspend fun <R> callService(service: NoInputService<R>): R {
        return callService(service, Unit)
    }
}