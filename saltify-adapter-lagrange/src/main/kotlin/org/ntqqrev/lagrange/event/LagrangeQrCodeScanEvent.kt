package org.ntqqrev.lagrange.event

import kotlinx.datetime.Instant
import org.ntqqrev.saltify.Context
import org.ntqqrev.saltify.event.Event

class LagrangeQrCodeScanEvent(
    ctx: Context,
    time: Instant,
    val qrCodePng: ByteArray,
    val qrCodeUrl: String,
) : Event(ctx, time)