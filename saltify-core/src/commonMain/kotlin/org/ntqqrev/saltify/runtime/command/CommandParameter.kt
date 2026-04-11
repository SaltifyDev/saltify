package org.ntqqrev.saltify.runtime.command

import kotlin.reflect.KClass

/**
 * 指令参数
 */
public class CommandParameter<T : Any>(
    internal val transform: (String) -> T?,
    public val type: KClass<T>,
    public val name: String,
    public val description: String = "",
    internal val isGreedy: Boolean = false
)
