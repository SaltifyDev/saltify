package org.ntqqrev.saltify.dsl

import io.ktor.util.logging.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import org.ntqqrev.milky.Event
import org.ntqqrev.milky.IncomingMessage
import org.ntqqrev.milky.OutgoingSegment
import org.ntqqrev.saltify.annotation.ContextParametersMigrationNeeded
import org.ntqqrev.saltify.annotation.SaltifyDsl
import org.ntqqrev.saltify.core.SaltifyApplication
import org.ntqqrev.saltify.core.recallGroupMessage
import org.ntqqrev.saltify.core.recallPrivateMessage
import org.ntqqrev.saltify.entity.SaltifyCommandRequirementContext
import org.ntqqrev.saltify.extension.respond
import org.ntqqrev.saltify.model.CommandError
import org.ntqqrev.saltify.model.CommandRequirement
import org.ntqqrev.saltify.model.milky.SendMessageOutput
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@SaltifyDsl
public class SaltifyCommandContext internal constructor() {
    public val parameter: SaltifyParameterBuilder = SaltifyParameterBuilder(this)
    @PublishedApi
    internal val parameters: MutableList<SaltifyCommandParamDef<*>> = mutableListOf()
    internal val subCommands = mutableListOf<Pair<String, SaltifyCommandContext>>()
    internal var executionBlock: (suspend SaltifyCommandExecutionContext.() -> Unit)? = null

    /**
     * 指令的描述信息。
     */
    public var description: String = ""
    internal var groupExecutionBlock: (suspend SaltifyCommandExecutionContext.() -> Unit)? = null
    internal var privateExecutionBlock: (suspend SaltifyCommandExecutionContext.() -> Unit)? = null
    internal var failureBlock: (suspend SaltifyCommandExecutionContext.(CommandError) -> Unit)? = null
    internal var requirementBlock: (SaltifyCommandRequirementContext.() -> CommandRequirement)? = null

    /**
     * 注册一个子指令。
     */
    public fun subCommand(name: String, block: SaltifyCommandContext.() -> Unit) {
        subCommands.add(name to SaltifyCommandContext().apply(block))
    }

    /**
     * 定义指令执行要求。若不满足，静默返回。
     */
    public fun require(block: SaltifyCommandRequirementContext.() -> CommandRequirement) {
        this.requirementBlock = block
    }

    /**
     * 设置通用的指令执行逻辑。
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
     * 当指令**解析**失败时执行的逻辑。
     */
    public fun onFailure(block: suspend SaltifyCommandExecutionContext.(CommandError) -> Unit) {
        failureBlock = block
    }
}

public class SaltifyParameterBuilder(@PublishedApi internal val context: SaltifyCommandContext) {
    /**
     * 定义一个指令参数。请搭配 [SaltifyCommandExecutionContext.value] 使用。
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

public class SaltifyCommandExecutionContext(
    public val client: SaltifyApplication,
    public val event: Event.MessageReceive,
    commandName: String,
    private val argumentMap: Map<SaltifyCommandParamDef<*>, Any?>
) {
    public val logger: Logger = KtorSimpleLogger("Saltify/cmd:$commandName")

    /**
     * 获取已解析的参数值。
     */
    @Suppress("UNCHECKED_CAST")
    public val <T : Any> SaltifyCommandParamDef<T>.value: T
        get() = (argumentMap[this] as? ParameterParseResult.Success<T>)?.value!!

    /**
     * 响应指令。
     */
    public suspend fun respond(
        block: MutableList<OutgoingSegment>.() -> Unit
    ): SendMessageOutput = event.respond(client, block)

    /**
     * 响应指令，并在指定延迟后撤回消息。
     */
    @ContextParametersMigrationNeeded
    public suspend inline fun respondWithRecall(
        delay: Duration,
        noinline block: MutableList<OutgoingSegment>.() -> Unit
    ) {
        val output = respond(block)
        delay(delay)
        when (val data = event.data) {
            is IncomingMessage.Group -> client.recallGroupMessage(data.peerId, output.messageSeq)
            else -> client.recallPrivateMessage(data.peerId, output.messageSeq)
        }
    }

    /**
     * 获取由指令触发者发送的下一条消息事件。超时返回 null。
     */
    public suspend fun awaitNextMessage(timeout: Duration = 30.seconds): Event.MessageReceive? {
        val messageFlow = client.eventFlow.filterIsInstance<Event.MessageReceive>()

        return withTimeoutOrNull(timeout) {
            messageFlow.first { nextEvent ->
                when (val contextData = event.data) {
                    is IncomingMessage.Group -> {
                        nextEvent.data is IncomingMessage.Group &&
                            (nextEvent.data as IncomingMessage.Group).group.groupId == contextData.group.groupId &&
                            nextEvent.senderId == event.senderId
                    }
                    else -> nextEvent.senderId == event.senderId
                }
            }
        }
    }
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
