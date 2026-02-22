package org.ntqqrev.saltify.extension

import org.ntqqrev.saltify.dsl.SaltifyCommandContext
import org.ntqqrev.saltify.dsl.SaltifyCommandExecutionContext
import org.ntqqrev.saltify.dsl.SaltifyCommandParamDef

/**
 * 定义一个指令参数。请搭配 [SaltifyCommandExecutionContext.capture] 使用。
 */
public inline fun <reified T : Any> SaltifyCommandContext.parameter(
    name: String,
    description: String = ""
): SaltifyCommandParamDef<T> = parameter(T::class, name, description)
