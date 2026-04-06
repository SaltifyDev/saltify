package org.ntqqrev.saltify.entity.env

import org.ntqqrev.saltify.core.SaltifyApplication

public open class ApplicationEnvironment(
    public open val client: SaltifyApplication
)

context(ctx: ApplicationEnvironment)
public val client: SaltifyApplication get() = ctx.client
