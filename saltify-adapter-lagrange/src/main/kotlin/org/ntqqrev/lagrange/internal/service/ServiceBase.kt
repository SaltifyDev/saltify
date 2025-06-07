package org.ntqqrev.lagrange.internal.service

import org.ntqqrev.lagrange.internal.LagrangeClient

internal interface Service<T, R> {
    val cmd: String
    fun build(client: LagrangeClient, payload: T): ByteArray
    fun parse(client: LagrangeClient, payload: ByteArray): R
}

internal interface NoInputService<R> : Service<Unit, R>

internal abstract class NoOutputService<T> : Service<T, Unit> {
    override fun parse(client: LagrangeClient, payload: ByteArray) = Unit
}