package org.ntqqrev.saltify.extension

import org.ntqqrev.saltify.core.text
import org.ntqqrev.saltify.dsl.SaltifyCommandExecutionContext
import org.ntqqrev.saltify.dsl.SaltifyCommandParamDef
import org.ntqqrev.saltify.dsl.SaltifyParameterBuilder
import org.ntqqrev.saltify.model.milky.SendMessageOutput

/**
 * 定义一个指令参数。请搭配 [SaltifyCommandExecutionContext.value] 使用。
 */
public fun SaltifyParameterBuilder.string(name: String, desc: String = ""): SaltifyCommandParamDef<String> =
    from(name, desc) { it }

/**
 * 定义一个指令参数。请搭配 [SaltifyCommandExecutionContext.value] 使用。
 */
public fun SaltifyParameterBuilder.int(name: String, desc: String = ""): SaltifyCommandParamDef<Int> =
    from(name, desc) { it.toIntOrNull() }

/**
 * 定义一个指令参数。请搭配 [SaltifyCommandExecutionContext.value] 使用。
 */
public fun SaltifyParameterBuilder.long(name: String, desc: String = ""): SaltifyCommandParamDef<Long> =
    from(name, desc) { it.toLongOrNull() }

/**
 * 定义一个指令参数。请搭配 [SaltifyCommandExecutionContext.value] 使用。
 */
public fun SaltifyParameterBuilder.boolean(name: String, desc: String = ""): SaltifyCommandParamDef<Boolean> =
    from(name, desc) { it.toBooleanStrictOrNull() }

/**
 * 定义一个指令参数。请搭配 [SaltifyCommandExecutionContext.value] 使用。
 */
public fun SaltifyParameterBuilder.double(name: String, desc: String = ""): SaltifyCommandParamDef<Double> =
    from(name, desc) { it.toDoubleOrNull() }

/**
 * 定义一个贪婪字符串参数。该参数会捕获剩余的**所有**文本内容。请搭配 [SaltifyCommandExecutionContext.value] 使用。
 */
public fun SaltifyParameterBuilder.greedyString(name: String, desc: String = ""): SaltifyCommandParamDef<String> =
    from(name, desc, isGreedy = true) { it }

/**
 * 响应事件。这是用于返回纯文本的简写。
 */
public suspend inline fun SaltifyCommandExecutionContext.respond(
    text: Any?
): SendMessageOutput = respond { text(text.toString()) }
