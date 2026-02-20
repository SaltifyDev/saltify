package org.ntqqrev.saltify.util.coroutine

import org.ntqqrev.saltify.model.SaltifyComponentType
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * Saltify 的协程组件
 */
public class SaltifyComponent(
    public val type: SaltifyComponentType,
    public val name: String
) : AbstractCoroutineContextElement(SaltifyComponent) {
    public companion object Key : CoroutineContext.Key<SaltifyComponent>
}

/**
 * 获取当前协程上下文中的 SaltifyComponent 实例。
 */
public val CoroutineContext.saltifyComponent: SaltifyComponent?
    get() = this[SaltifyComponent]
