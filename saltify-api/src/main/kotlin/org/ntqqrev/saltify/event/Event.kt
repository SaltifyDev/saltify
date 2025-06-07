package org.ntqqrev.saltify.event

import kotlinx.datetime.Instant
import org.ntqqrev.saltify.Context
import org.ntqqrev.saltify.Entity

abstract class Event(
    override val ctx: Context,

    /**
     * The time when the event was signaled.
     */
    val time: Instant
) : Entity