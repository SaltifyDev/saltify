package org.ntqqrev.saltify.dsl

import io.ktor.util.logging.*
import org.ntqqrev.milky.Event
import org.ntqqrev.saltify.annotation.SaltifyDsl
import org.ntqqrev.saltify.core.SaltifyApplication
import org.ntqqrev.saltify.entity.CommandRequirementMatch
import org.ntqqrev.saltify.entity.env.EventEnvironment
import org.ntqqrev.saltify.model.CommandError
import org.ntqqrev.saltify.model.CommandRequirement
import kotlin.reflect.KClass

@SaltifyDsl
public class SaltifyCommandContext internal constructor() {
    public var description: String = ""
    public val parameter: SaltifyParameterBuilder = SaltifyParameterBuilder(this)

    @PublishedApi
    internal val parameters: MutableList<SaltifyCommandParamDef<*>> = mutableListOf()
    internal val subCommands = mutableListOf<Pair<String, SaltifyCommandContext>>()

    internal var executionBlock: (suspend CommandExecutionContext.() -> Unit)? = null
    internal var groupExecutionBlock: (suspend CommandExecutionContext.() -> Unit)? = null
    internal var privateExecutionBlock: (suspend CommandExecutionContext.() -> Unit)? = null
    internal var failureBlock: (suspend CommandExecutionContext.(CommandError) -> Unit)? = null
    internal var requirementBlock: (CommandRequirementMatch.() -> CommandRequirement)? = null

    /**
     * 注册一个子指令。
     */
    public fun subCommand(name: String, block: SaltifyCommandContext.() -> Unit) {
        subCommands.add(name to SaltifyCommandContext().apply(block))
    }

    /**
     * 定义指令执行要求。若不满足，静默返回。
     */
    public fun require(block: CommandRequirementMatch.() -> CommandRequirement) {
        this.requirementBlock = block
    }

    /**
     * 设置通用的指令执行逻辑。
     */
    public fun onExecute(block: suspend CommandExecutionContext.() -> Unit) {
        executionBlock = block
    }

    /**
     * 设置仅在群聊中触发的执行逻辑。优先级高于 [onExecute]，定义后在群聊不会使用 [onExecute]。
     */
    public fun onGroupExecute(block: suspend CommandExecutionContext.() -> Unit) {
        groupExecutionBlock = block
    }

    /**
     * 设置仅在私聊中触发的执行逻辑。优先级高于 [onExecute]，定义后在私聊不会使用 [onExecute]。
     */
    public fun onPrivateExecute(block: suspend CommandExecutionContext.() -> Unit) {
        privateExecutionBlock = block
    }

    /**
     * 当指令**解析**失败时执行的逻辑。
     */
    public fun onFailure(block: suspend CommandExecutionContext.(CommandError) -> Unit) {
        failureBlock = block
    }
}

public class SaltifyParameterBuilder(@PublishedApi internal val context: SaltifyCommandContext) {
    /**
     * 定义一个指令参数。请搭配 [CommandExecutionContext.value] 使用。
     *
     * @param isGreedy 是否是贪婪参数，即是否包含后面的所有剩余文本。
     * @param transform 将原始文本转化为目标类型的函数。
     */
    public inline fun <reified T : Any> from(
        name: String,
        description: String,
        isGreedy: Boolean = false,
        noinline transform: (String) -> T?
    ): SaltifyCommandParamDef<T> = SaltifyCommandParamDef(transform, T::class, name, description, isGreedy).also {
            context.parameters.add(it)
        }
}

public class CommandExecutionContext(
    public override val client: SaltifyApplication,
    public override val event: Event.MessageReceive,
    commandName: String,
    private val argumentMap: Map<SaltifyCommandParamDef<*>, Any?>
) : EventEnvironment<Event.MessageReceive>(event, client) {
    public val logger: Logger = KtorSimpleLogger("Saltify/cmd:$commandName")

    /**
     * 获取已解析的参数值。
     */
    @Suppress("UNCHECKED_CAST")
    public val <T : Any> SaltifyCommandParamDef<T>.value: T
        get() = (argumentMap[this] as? ParameterParseResult.Success<T>)?.value!!
}

/**
 * 指令参数
 */
public class SaltifyCommandParamDef<T : Any>(
    internal val transform: (String) -> T?,
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
