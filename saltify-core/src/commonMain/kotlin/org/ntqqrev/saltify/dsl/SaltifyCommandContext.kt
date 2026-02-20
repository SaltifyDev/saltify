package org.ntqqrev.saltify.dsl

import org.ntqqrev.milky.Event
import org.ntqqrev.milky.OutgoingSegment
import org.ntqqrev.saltify.annotation.SaltifyDsl
import org.ntqqrev.saltify.core.SaltifyApplication
import org.ntqqrev.saltify.entity.CommandError
import org.ntqqrev.saltify.extension.respond
import kotlin.reflect.KClass

@SaltifyDsl
public class SaltifyCommandContext internal constructor() {
    internal val subCommands = mutableListOf<Pair<String, SaltifyCommandContext>>()
    internal val parameters = mutableListOf<SaltifyCommandParamDef<*>>()
    internal var executionBlock: (suspend SaltifyCommandExecutionContext.() -> Unit)? = null
    internal var groupExecutionBlock: (suspend SaltifyCommandExecutionContext.() -> Unit)? = null
    internal var privateExecutionBlock: (suspend SaltifyCommandExecutionContext.() -> Unit)? = null
    internal var failureBlock: (suspend SaltifyCommandExecutionContext.(CommandError) -> Unit)? = null

    /**
     * 注册一个子命令。
     */
    public fun subCommand(name: String, block: SaltifyCommandContext.() -> Unit) {
        subCommands.add(name to SaltifyCommandContext().apply(block))
    }

    /**
     * 定义一个命令参数。请搭配 [SaltifyCommandExecutionContext.capture] 使用。
     */
    public fun <T : Any> parameter(
        type: KClass<T>,
        name: String,
        description: String = ""
    ): SaltifyCommandParamDef<T> {
        return SaltifyCommandParamDef(type, name, description).also { parameters.add(it) }
    }

    /**
     * 定义一个贪婪字符串参数。该参数会捕获剩余的**所有**文本内容。请搭配 [SaltifyCommandExecutionContext.capture] 使用。
     */
    public fun greedyStringParameter(
        name: String,
        description: String = ""
    ): SaltifyCommandParamDef<String> {
        return SaltifyCommandParamDef(String::class, name, description, isGreedy = true).also { parameters.add(it) }
    }

    /**
     * 设置通用的命令执行逻辑。
     */
    public fun onExecute(block: suspend SaltifyCommandExecutionContext.() -> Unit) {
        executionBlock = block
    }

    /**
     * 设置仅在群聊中触发的执行逻辑。优先级高于 [onExecute]，定义后在群聊不会使用 [onExecute]。
     */
    public fun onGroupExecute(block: suspend SaltifyCommandExecutionContext.() -> Unit) {
        groupExecutionBlock = block
    }

    /**
     * 设置仅在私聊中触发的执行逻辑。优先级高于 [onExecute]，定义后在群聊不会使用 [onExecute]。
     */
    public fun onPrivateExecute(block: suspend SaltifyCommandExecutionContext.() -> Unit) {
        privateExecutionBlock = block
    }

    /**
     * 当命令**解析**失败时执行的逻辑。
     */
    public fun onFailure(block: suspend SaltifyCommandExecutionContext.(CommandError) -> Unit) {
        failureBlock = block
    }
}

/**
 * 定义一个命令参数。请搭配 [SaltifyCommandExecutionContext.capture] 使用。
 */
public inline fun <reified T : Any> SaltifyCommandContext.parameter(
    name: String,
    description: String = ""
): SaltifyCommandParamDef<T> = parameter(T::class, name, description)

public class SaltifyCommandExecutionContext(
    public val client: SaltifyApplication,
    public val event: Event.MessageReceive,
    private val argumentMap: Map<SaltifyCommandParamDef<*>, Any?>
) {
    /**
     * 获取已解析的参数值。你可能更需要的是 [SaltifyCommandParamDef.value]。
     */
    @Suppress("UNCHECKED_CAST")
    public fun <T : Any> capture(capturer: SaltifyCommandParamDef<T>): T {
        val result = argumentMap[capturer]
        return (result as? ParameterParseResult.Success<T>)?.value
            ?: error("Parameter ${capturer.name} accessed without validation")
    }

    /**
     * 获取已解析的参数值。这是 [capture] 的简写形式。
     */
    @Suppress("UNCHECKED_CAST")
    public val <T : Any> SaltifyCommandParamDef<T>.value: T
        get() = capture(this)

    /**
     * 回复当前命令。
     */
    public suspend fun respond(block: MutableList<OutgoingSegment>.() -> Unit) {
        event.respond(client, block)
    }
}

/**
 * 命令参数。
 */
public class SaltifyCommandParamDef<T : Any>(
    public val type: KClass<T>,
    public val name: String,
    public val description: String = "",
    internal val isGreedy: Boolean = false
)

internal sealed class ParameterParseResult<out T : Any> {
    data class Success<T : Any>(val value: T) : ParameterParseResult<T>()
    data class InvalidParam(val rawValue: String) : ParameterParseResult<Unit>()
    object MissingParam : ParameterParseResult<Unit>()
}
