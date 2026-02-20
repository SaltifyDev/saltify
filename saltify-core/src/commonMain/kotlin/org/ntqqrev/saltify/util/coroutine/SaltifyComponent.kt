package org.ntqqrev.saltify.util.coroutine

import org.ntqqrev.saltify.entity.SaltifyComponentType
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

public class SaltifyComponent(
    public val type: SaltifyComponentType,
    public val name: String
) : AbstractCoroutineContextElement(SaltifyComponent) {
    public companion object Key : CoroutineContext.Key<SaltifyComponent>
}

public val CoroutineContext.saltifyComponent: SaltifyComponent?
    get() = this[SaltifyComponent]
