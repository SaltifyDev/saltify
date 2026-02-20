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

    public fun subCommand(name: String, block: SaltifyCommandContext.() -> Unit) {
        subCommands.add(name to SaltifyCommandContext().apply(block))
    }

    public fun <T : Any> parameter(
        type: KClass<T>,
        name: String,
        description: String = ""
    ): SaltifyCommandParamDef<T> {
        return SaltifyCommandParamDef(type, name, description).also { parameters.add(it) }
    }

    public fun greedyStringParameter(
        name: String,
        description: String = ""
    ): SaltifyCommandParamDef<String> {
        return SaltifyCommandParamDef(String::class, name, description, isGreedy = true).also { parameters.add(it) }
    }

    public fun onExecute(block: suspend SaltifyCommandExecutionContext.() -> Unit) {
        executionBlock = block
    }

    public fun onGroupExecute(block: suspend SaltifyCommandExecutionContext.() -> Unit) {
        groupExecutionBlock = block
    }

    public fun onPrivateExecute(block: suspend SaltifyCommandExecutionContext.() -> Unit) {
        privateExecutionBlock = block
    }

    public fun onFailure(block: suspend SaltifyCommandExecutionContext.(CommandError) -> Unit) {
        failureBlock = block
    }
}

public inline fun <reified T : Any> SaltifyCommandContext.parameter(
    name: String,
    description: String = ""
): SaltifyCommandParamDef<T> = parameter(T::class, name, description)

public class SaltifyCommandExecutionContext(
    public val client: SaltifyApplication,
    public val event: Event.MessageReceive,
    private val argumentMap: Map<SaltifyCommandParamDef<*>, Any?>
) {
    @Suppress("UNCHECKED_CAST")
    public fun <T : Any> capture(capturer: SaltifyCommandParamDef<T>): T {
        val result = argumentMap[capturer]
        return (result as? ParameterParseResult.Success<T>)?.value
            ?: error("Parameter ${capturer.name} accessed without validation")
    }

    @Suppress("UNCHECKED_CAST")
    public val <T : Any> SaltifyCommandParamDef<T>.value: T
        get() = capture(this)

    public suspend fun respond(block: MutableList<OutgoingSegment>.() -> Unit) {
        event.respond(client, block)
    }
}

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
