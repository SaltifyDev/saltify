@file:Suppress("TooManyFunctions")

package org.ntqqrev.saltify.extension

import org.ntqqrev.milky.IncomingSegment
import org.ntqqrev.saltify.dsl.SaltifyParameterBuilder
import org.ntqqrev.saltify.runtime.command.CommandParameter
import org.ntqqrev.saltify.runtime.context.CommandExecutionContext

/**
 * 定义一个指令参数。请搭配 [CommandExecutionContext.value] 使用。
 */
public fun SaltifyParameterBuilder.string(name: String, desc: String = ""): CommandParameter<String> =
    from(name, desc) { (it as? IncomingSegment.Text)?.text }

/**
 * 定义一个指令参数。请搭配 [CommandExecutionContext.value] 使用。
 */
public fun SaltifyParameterBuilder.int(name: String, desc: String = ""): CommandParameter<Int> =
    from(name, desc) { (it as? IncomingSegment.Text)?.text?.toIntOrNull() }

/**
 * 定义一个指令参数。请搭配 [CommandExecutionContext.value] 使用。
 */
public fun SaltifyParameterBuilder.long(name: String, desc: String = ""): CommandParameter<Long> =
    from(name, desc) { (it as? IncomingSegment.Text)?.text?.toLongOrNull() }

/**
 * 定义一个指令参数。请搭配 [CommandExecutionContext.value] 使用。
 */
public fun SaltifyParameterBuilder.boolean(name: String, desc: String = ""): CommandParameter<Boolean> =
    from(name, desc) { (it as? IncomingSegment.Text)?.text?.toBooleanStrictOrNull() }

/**
 * 定义一个指令参数。请搭配 [CommandExecutionContext.value] 使用。
 */
public fun SaltifyParameterBuilder.double(name: String, desc: String = ""): CommandParameter<Double> =
    from(name, desc) { (it as? IncomingSegment.Text)?.text?.toDoubleOrNull() }

/**
 * 定义一个贪婪字符串参数。该参数会捕获剩余的**所有**文本内容。请搭配 [CommandExecutionContext.value] 使用。
 */
public fun SaltifyParameterBuilder.greedyString(name: String, desc: String = ""): CommandParameter<String> =
    from(name, desc, isGreedy = true) { (it as? IncomingSegment.Text)?.text }

/**
 * 定义一个指令参数。请搭配 [CommandExecutionContext.value] 使用。
 */
public fun SaltifyParameterBuilder.image(name: String, desc: String = ""): CommandParameter<IncomingSegment.Image.Data> =
    from(name, desc) { (it as? IncomingSegment.Image)?.data }

/**
 * 定义一个指令参数。请搭配 [CommandExecutionContext.value] 使用。
 */
public fun SaltifyParameterBuilder.mention(name: String, desc: String = ""): CommandParameter<IncomingSegment.Mention.Data> =
    from(name, desc) { (it as? IncomingSegment.Mention)?.data }

/**
 * 定义一个指令参数。请搭配 [CommandExecutionContext.value] 使用。
 */
public fun SaltifyParameterBuilder.mentionAll(name: String, desc: String = ""): CommandParameter<Unit> =
    from(name, desc) { if (it is IncomingSegment.MentionAll) Unit else null }

/**
 * 定义一个指令参数。请搭配 [CommandExecutionContext.value] 使用。
 */
public fun SaltifyParameterBuilder.face(name: String, desc: String = ""): CommandParameter<IncomingSegment.Face.Data> =
    from(name, desc) { (it as? IncomingSegment.Face)?.data }

/**
 * 定义一个指令参数。请搭配 [CommandExecutionContext.value] 使用。
 */
public fun SaltifyParameterBuilder.reply(name: String, desc: String = ""): CommandParameter<IncomingSegment.Reply.Data> =
    from(name, desc) { (it as? IncomingSegment.Reply)?.data }
