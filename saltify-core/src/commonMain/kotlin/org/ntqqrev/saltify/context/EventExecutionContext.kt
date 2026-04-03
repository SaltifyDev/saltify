package org.ntqqrev.saltify.context

import org.ntqqrev.milky.Event
import org.ntqqrev.saltify.core.SaltifyApplication

public open class EventExecutionContext<out T : Event>(
    public open val event: T,
    public open val client: SaltifyApplication
)

context(ctx: EventExecutionContext<T>)
public val <T : Event> event: T get() = ctx.event

context(ctx: EventExecutionContext<T>)
public val <T : Event> client: SaltifyApplication get() = ctx.client
