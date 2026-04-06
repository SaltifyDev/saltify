package org.ntqqrev.saltify.entity.env

import org.ntqqrev.milky.Event
import org.ntqqrev.saltify.core.SaltifyApplication

public open class EventEnvironment<out T : Event>(
    public open val event: T,
    public override val client: SaltifyApplication
) : ApplicationEnvironment(client)

context(ctx: EventEnvironment<T>)
public val <T : Event> event: T get() = ctx.event

context(ctx: EventEnvironment<T>)
public val <T : Event> client: SaltifyApplication get() = ctx.client
