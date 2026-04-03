package org.ntqqrev.saltify.context

import org.ntqqrev.saltify.core.SaltifyApplication

public open class ApplicationExecutionContext(
    public open val client: SaltifyApplication
)

context(ctx: ApplicationExecutionContext)
public val client: SaltifyApplication get() = ctx.client
