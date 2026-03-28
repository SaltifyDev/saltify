package org.ntqqrev.saltify.extension

import org.ntqqrev.saltify.core.text
import org.ntqqrev.saltify.dsl.SaltifyCommandContext
import org.ntqqrev.saltify.dsl.SaltifyCommandExecutionContext
import org.ntqqrev.saltify.dsl.SaltifyCommandParamDef
import org.ntqqrev.saltify.model.milky.SendMessageOutput

/**
 * 定义一个指令参数。请搭配 [SaltifyCommandExecutionContext.capture] 使用。
 */
public inline fun <reified T : Any> SaltifyCommandContext.parameter(
    name: String,
    description: String = ""
): SaltifyCommandParamDef<T> = parameter(T::class, name, description)

/**
 * 响应事件。这是用于返回纯文本的简写。
 */
public suspend inline fun SaltifyCommandExecutionContext.respond(
    text: Any?
): SendMessageOutput = respond { text(text.toString()) }
