package org.ntqqrev.milky.dsl

import org.ntqqrev.milky.Event
import org.ntqqrev.milky.OutgoingSegment
import org.ntqqrev.milky.annotation.MilkyDsl
import org.ntqqrev.milky.core.MilkyClient
import org.ntqqrev.milky.entity.CommandError
import kotlin.reflect.KClass

@MilkyDsl
public class MilkyCommandDsl internal constructor() {
    internal val subCommands = mutableListOf<Pair<String, MilkyCommandDsl>>()
    internal val parameters = mutableListOf<MilkyParamCapturer<*>>()
    internal var executionBlock: (suspend MilkyCommandExecution.() -> Unit)? = null
    internal var groupExecutionBlock: (suspend MilkyCommandExecution.() -> Unit)? = null
    internal var privateExecutionBlock: (suspend MilkyCommandExecution.() -> Unit)? = null
    internal var failureBlock: (suspend MilkyCommandExecution.(CommandError) -> Unit)? = null

    public fun subCommand(name: String, block: MilkyCommandDsl.() -> Unit) {
        subCommands.add(name to MilkyCommandDsl().apply(block))
    }

    public fun <T : Any> parameter(
        type: KClass<T>,
        name: String,
        description: String = ""
    ): MilkyParamCapturer<T> {
        return MilkyParamCapturer(type, name, description).also { parameters.add(it) }
    }

    public fun greedyStringParameter(
        name: String,
        description: String = ""
    ): MilkyParamCapturer<String> {
        return MilkyParamCapturer(String::class, name, description, isGreedy = true).also { parameters.add(it) }
    }

    public fun onExecute(block: suspend MilkyCommandExecution.() -> Unit) {
        executionBlock = block
    }

    public fun onGroupExecute(block: suspend MilkyCommandExecution.() -> Unit) {
        groupExecutionBlock = block
    }

    public fun onPrivateExecute(block: suspend MilkyCommandExecution.() -> Unit) {
        privateExecutionBlock = block
    }

    public fun onFailure(block: suspend MilkyCommandExecution.(CommandError) -> Unit) {
        failureBlock = block
    }
}

public inline fun <reified T : Any> MilkyCommandDsl.parameter(
    name: String,
    description: String = ""
): MilkyParamCapturer<T> = parameter(T::class, name, description)

public class MilkyCommandExecution(
    public val client: MilkyClient,
    public val event: Event.MessageReceive,
    private val argumentMap: Map<MilkyParamCapturer<*>, Any?>
) {
    @Suppress("UNCHECKED_CAST")
    public fun <T : Any> capture(capturer: MilkyParamCapturer<T>): T {
        val result = argumentMap[capturer]
        return (result as? ParameterParseResult.Success<T>)?.value
            ?: error("Parameter ${capturer.name} accessed without validation")
    }

    @Suppress("UNCHECKED_CAST")
    public val <T : Any> MilkyParamCapturer<T>.value: T
        get() = capture(this)

    public suspend fun respond(block: MutableList<OutgoingSegment>.() -> Unit) {
        with(MilkyPluginDsl(client, client.clientScope)) {
            event.reply(block)
        }
    }
}

public class MilkyParamCapturer<T : Any>(
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
