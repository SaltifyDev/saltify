package org.ntqqrev.saltify.extension

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import org.ntqqrev.milky.Event
import org.ntqqrev.milky.IncomingMessage
import org.ntqqrev.milky.IncomingSegment
import org.ntqqrev.saltify.core.SaltifyApplication
import org.ntqqrev.saltify.dsl.ParameterParseResult
import org.ntqqrev.saltify.dsl.SaltifyCommandContext
import org.ntqqrev.saltify.dsl.CommandExecutionContext
import org.ntqqrev.saltify.dsl.SaltifyCommandParamDef
import org.ntqqrev.saltify.entity.RegisteredCommand
import org.ntqqrev.saltify.entity.RegisteredSubCommand
import org.ntqqrev.saltify.entity.SaltifyBotConfig
import org.ntqqrev.saltify.entity.CommandRequirementMatch
import org.ntqqrev.saltify.entity.env.ApplicationEnvironment
import org.ntqqrev.saltify.entity.env.EventEnvironment
import org.ntqqrev.saltify.entity.env.client
import org.ntqqrev.saltify.entity.env.event
import org.ntqqrev.saltify.model.CommandError
import org.ntqqrev.saltify.model.SaltifyComponentType
import org.ntqqrev.saltify.util.coroutine.runCatchingToExceptionFlow
import org.ntqqrev.saltify.util.coroutine.saltifyComponent
import kotlin.reflect.KClass
import kotlin.time.Clock

/**
 * 注册一个事件监听器。
 */
context(_: ApplicationEnvironment)
public inline fun <reified T : Event> on(
    scope: CoroutineScope = client.extensionScope,
    crossinline block: suspend context(EventEnvironment<T>) () -> Unit
): Job = scope.launch {
    client.eventFlow
        .filterIsInstance<T>()
        .collect {
            launch {
                runCatchingToExceptionFlow {
                    context(EventEnvironment(it, client)) { block() }
                }
            }
        }
}

/**
 * 注册一个消息正则匹配监听器。
 */
context(_: ApplicationEnvironment)
public inline fun regex(
    regex: String,
    scope: CoroutineScope = client.extensionScope,
    crossinline block: suspend context(EventEnvironment<Event.MessageReceive>) (matches: Sequence<MatchResult>) -> Unit
): Job {
    val regex = Regex(regex)

    return on<Event.MessageReceive>(scope) {
        val text = event.segments.filterIsInstance<IncomingSegment.Text>()
            .joinToString("") { it.text }

        val matches = regex.findAll(text)
        if (matches.any()) block(matches)
    }
}

private val spaceRegex = Regex("\\s+")

/**
 * 注册一个指令。
 */
context(_: ApplicationEnvironment)
public fun command(
    name: String,
    prefix: String = SaltifyBotConfig.commandPrefix,
    scope: CoroutineScope = client.extensionScope,
    builder: SaltifyCommandContext.() -> Unit
): Job {
    val rootDsl = SaltifyCommandContext().apply(builder)

    val component = scope.coroutineContext.saltifyComponent
    val pluginName = if (component?.type == SaltifyComponentType.Plugin) component.name else null
    client.commandRegistry.add(
        RegisteredCommand(
            name = name,
            prefix = prefix,
            description = rootDsl.description,
            parameters = rootDsl.parameters.toList(),
            subCommands = rootDsl.subCommands.map { (subName, subCtx) ->
                subCtx.toSubCommandInfo(subName)
            },
            pluginName = pluginName
        )
    )

    return on<Event.MessageReceive>(scope) {
        val rawText = event.segments.filterIsInstance<IncomingSegment.Text>()
            .joinToString("") { it.text }
            .trim()

        val tokens = if (rawText.isEmpty()) emptyList() else rawText.split(spaceRegex)
        if (tokens.isEmpty() || tokens[0] != "$prefix$name") return@on

        executeCommand(rootDsl, tokens.drop(1), client, event, name)
    }
}

private suspend fun executeCommand(
    dsl: SaltifyCommandContext,
    tokens: List<String>,
    client: SaltifyApplication,
    event: Event.MessageReceive,
    name: String
) {
    val argumentMap = mutableMapOf<SaltifyCommandParamDef<*>, ParameterParseResult<Any>>()
    val execution = CommandExecutionContext(event, client, name, argumentMap)

    dsl.requirementBlock?.let { block ->
        val requirement = CommandRequirementMatch(execution).block()
        if (!requirement.satisfies()) return
    }

    if (tokens.isNotEmpty()) {
        val subName = tokens[0]
        val subCommand = dsl.subCommands.find { it.first == subName }
        if (subCommand != null) {
            executeCommand(subCommand.second, tokens.drop(1), client, event, "$name $subName")
            return
        }
    }

    val errors = mutableListOf<CommandError>()
    val currentTokens = tokens.toMutableList()

    for (param in dsl.parameters) {
        val result = when {
            currentTokens.isEmpty() -> ParameterParseResult.MissingParam
            param.isGreedy -> {
                val value = currentTokens.joinToString(" ")
                currentTokens.clear()
                ParameterParseResult.Success(value)
            }
            else -> {
                val rawValue = currentTokens.removeFirst()
                convertValue(rawValue, param.type)?.let { ParameterParseResult.Success(it) }
                    ?: ParameterParseResult.InvalidParam(rawValue)
            }
        }

        argumentMap[param] = result
        if (result is ParameterParseResult.MissingParam) errors.add(CommandError.MissingParam(param))
        if (result is ParameterParseResult.InvalidParam) errors.add(CommandError.InvalidParam(param, result.rawValue))
    }

    if (currentTokens.isNotEmpty()) errors.add(CommandError.TooManyArguments(currentTokens))

    if (errors.isNotEmpty()) {
        dsl.failureBlock?.invoke(execution, errors.first())
        return
    }

    val startInstant = Clock.System.now()
    execution.logger.info("${event.peerId} 触发了 $name 指令 (seq=${event.messageSeq})")

    when (event.data) {
        is IncomingMessage.Group -> dsl.groupExecutionBlock ?: dsl.executionBlock
        else -> dsl.privateExecutionBlock ?: dsl.executionBlock
    }?.invoke(execution)

    execution.logger.info("seq=${event.messageSeq} 处理完成, 用时 ${Clock.System.now() - startInstant}")
}

private fun SaltifyCommandContext.toSubCommandInfo(name: String): RegisteredSubCommand =
    RegisteredSubCommand(
        name = name,
        description = description,
        parameters = parameters.toList(),
        subCommands = subCommands.map { (subName, subCtx) -> subCtx.toSubCommandInfo(subName) }
    )

@Suppress("UNCHECKED_CAST")
private fun <T : Any> convertValue(value: String, type: KClass<T>): T? {
    return when (type) {
        String::class -> value as T
        Int::class -> value.toIntOrNull() as? T
        Long::class -> value.toLongOrNull() as? T
        Boolean::class -> value.toBooleanStrictOrNull() as? T
        Double::class -> value.toDoubleOrNull() as? T
        else -> null
    }
}
