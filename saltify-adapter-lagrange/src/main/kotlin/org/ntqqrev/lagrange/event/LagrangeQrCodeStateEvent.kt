package org.ntqqrev.lagrange.event

import kotlinx.datetime.Instant
import org.ntqqrev.saltify.Context
import org.ntqqrev.saltify.event.Event

class LagrangeQrCodeStateEvent(
    ctx: Context,
    time: Instant,
    val stateStr: String,
) : Event(ctx, time)