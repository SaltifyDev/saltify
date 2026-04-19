package org.ntqqrev.saltify.runtime.command

import org.ntqqrev.milky.IncomingSegment
import kotlin.reflect.KClass

/**
 * 指令参数
 */
public class CommandParameter<T : Any>(
    internal val transform: (IncomingSegment) -> T?,
    public val type: KClass<T>,
    public val name: String,
    public val description: String = "",
    internal val isGreedy: Boolean = false
)
