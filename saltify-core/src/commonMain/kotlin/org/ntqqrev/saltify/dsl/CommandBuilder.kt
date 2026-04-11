package org.ntqqrev.saltify.dsl

import org.ntqqrev.saltify.annotation.SaltifyDsl
import org.ntqqrev.saltify.runtime.command.CommandRequirementMatch
import org.ntqqrev.saltify.runtime.command.CommandParameter
import org.ntqqrev.saltify.runtime.context.CommandExecutionContext
import org.ntqqrev.saltify.model.command.CommandError
import org.ntqqrev.saltify.model.command.CommandRequirement

@SaltifyDsl
public class CommandBuilder internal constructor() {
    public val parameter: SaltifyParameterBuilder = SaltifyParameterBuilder(this)

    @PublishedApi
    internal val parameters: MutableList<CommandParameter<*>> = mutableListOf()
    internal val subCommands = mutableListOf<Pair<String, CommandBuilder>>()
    internal var executionBlock: (suspend CommandExecutionContext.() -> Unit)? = null

    /**
     * 指令的描述信息。
     */
    public var description: String = ""

    internal var groupExecutionBlock: (suspend CommandExecutionContext.() -> Unit)? = null
    internal var privateExecutionBlock: (suspend CommandExecutionContext.() -> Unit)? = null
    internal var failureBlock: (suspend CommandExecutionContext.(CommandError) -> Unit)? = null
    internal var requirementBlock: (CommandRequirementMatch.() -> CommandRequirement)? = null

    /**
     * 注册一个子指令。
     */
    public fun subCommand(name: String, block: CommandBuilder.() -> Unit) {
        subCommands.add(name to CommandBuilder().apply(block))
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
     * 设置仅在私聊中触发的执行逻辑。优先级高于 [onExecute]，定义后在群聊不会使用 [onExecute]。
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

public class SaltifyParameterBuilder(@PublishedApi internal val context: CommandBuilder) {
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
    ): CommandParameter<T> = CommandParameter(transform, T::class, name, description, isGreedy).also {
            context.parameters.add(it)
        }
}
