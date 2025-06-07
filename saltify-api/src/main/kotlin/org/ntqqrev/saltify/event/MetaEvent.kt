package org.ntqqrev.saltify.event

import kotlinx.datetime.Instant
import org.ntqqrev.saltify.Context

open class ContextStateChangeEvent(
    ctx: Context,
    time: Instant,

    /**
     * The previous state of the context.
     */
    val previousState: Context.State,

    /**
     * The new state of the context.
     */
    val newState: Context.State,
) : Event(ctx, time)