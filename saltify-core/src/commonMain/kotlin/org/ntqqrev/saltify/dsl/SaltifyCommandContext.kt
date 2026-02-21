package org.ntqqrev.saltify.dsl

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
import org.ntqqrev.saltify.extension.respond
import org.ntqqrev.saltify.model.CommandError
import org.ntqqrev.saltify.model.milky.SendMessageOutput
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

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
    @ContextParametersMigrationNeeded
    @Suppress("UNCHECKED_CAST")
    public val <T : Any> SaltifyCommandParamDef<T>.value: T
        get() = capture(this)

    /**
     * 响应命令。
     */
    public suspend fun respond(
        block: MutableList<OutgoingSegment>.() -> Unit
    ): SendMessageOutput = event.respond(client, block)

    /**
     * 响应命令，并在指定延迟后撤回消息。
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
     * 获取由命令触发者发送的下一条消息事件。超时返回 null。
     */
    public suspend fun awaitNextMessage(timeout: Duration = 30.seconds): Event.MessageReceive? {
        val messageFlow = client.eventFlow.filterIsInstance<Event.MessageReceive>()

        return withTimeoutOrNull(timeout) {
            messageFlow.first { nextEvent ->
                val nextData = nextEvent.data

                when (val contextData = event.data) {
                    is IncomingMessage.Group -> {
                        nextData is IncomingMessage.Group &&
                            nextData.group.groupId == contextData.group.groupId &&
                            nextData.senderId == contextData.senderId
                    }
                    else -> nextData.senderId == contextData.senderId
                }
            }
        }
    }
}

/**
 * 命令参数
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
