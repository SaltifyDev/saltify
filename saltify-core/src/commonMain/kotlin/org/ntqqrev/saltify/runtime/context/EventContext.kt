package org.ntqqrev.saltify.runtime.context

import org.ntqqrev.milky.Event
import org.ntqqrev.saltify.SaltifyApplication

public open class EventContext<out T : Event>(
    public open val event: T,
    public override val client: SaltifyApplication
) : ApplicationContext(client)
