package org.ntqqrev.saltify.extension

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.ntqqrev.milky.Event
import org.ntqqrev.milky.IncomingMessage
import org.ntqqrev.milky.IncomingSegment
import org.ntqqrev.saltify.core.SaltifyApplication
import org.ntqqrev.saltify.dsl.ParameterParseResult
import org.ntqqrev.saltify.dsl.SaltifyCommandContext
import org.ntqqrev.saltify.dsl.SaltifyCommandExecutionContext
import org.ntqqrev.saltify.dsl.SaltifyCommandParamDef
import org.ntqqrev.saltify.entity.CommandError
import kotlin.reflect.KClass

public inline fun <reified T : Event> SaltifyApplication.on(
    scope: CoroutineScope = extensionScope,
    crossinline block: suspend SaltifyApplication.(event: T) -> Unit
): Job = scope.launch {
    eventFlow.filter { it is T }.collect { block(it as T) }
}

public fun SaltifyApplication.command(
    name: String,
    prefix: String = "/",
    scope: CoroutineScope = extensionScope,
    builder: SaltifyCommandContext.() -> Unit
): Job {
    val rootDsl = SaltifyCommandContext().apply(builder)

    return on<Event.MessageReceive>(scope) { event ->
        val rawText = event.data.segments.filterIsInstance<IncomingSegment.Text>()
            .joinToString("") { it.data.text }
            .trim()

        if (rawText != "$prefix$name" && !rawText.startsWith("$prefix$name ")) return@on

        val content = rawText.removePrefix("$prefix$name").trim()
        val tokens = if (content.isEmpty()) emptyList() else content.split(Regex("\\s+"))

        executeCommand(rootDsl, tokens, this, event)
    }
}

private suspend fun executeCommand(
    dsl: SaltifyCommandContext,
    tokens: List<String>,
    client: SaltifyApplication,
    event: Event.MessageReceive
) {
    if (tokens.isNotEmpty()) {
        val subName = tokens[0]
        val subCommand = dsl.subCommands.find { it.first == subName }
        if (subCommand != null) {
            executeCommand(subCommand.second, tokens.drop(1), client, event)
            return
        }
    }

    val argumentMap = mutableMapOf<SaltifyCommandParamDef<*>, ParameterParseResult<Any>>()
    val errors = mutableListOf<CommandError>()
    var currentTokens = tokens

    for (param in dsl.parameters) {
        val result: ParameterParseResult<Any> = when {
            currentTokens.isEmpty() -> ParameterParseResult.MissingParam
            param.isGreedy -> {
                val value = currentTokens.joinToString(" ")
                currentTokens = emptyList()
                ParameterParseResult.Success(value)
            }

            else -> {
                val rawValue = currentTokens[0]
                currentTokens = currentTokens.drop(1)
                val converted = convertValue(rawValue, param.type)
                if (converted != null) {
                    ParameterParseResult.Success(converted)
                } else {
                    ParameterParseResult.InvalidParam(rawValue)
                }
            }
        }

        argumentMap[param] = result

        when (result) {
            is ParameterParseResult.MissingParam -> errors.add(CommandError.MissingParam(param))
            is ParameterParseResult.InvalidParam -> errors.add(CommandError.InvalidParam(param, result.rawValue))
            else -> {}
        }
    }

    val execution = SaltifyCommandExecutionContext(client, event, argumentMap)

    if (errors.isNotEmpty()) {
        val handler = dsl.failureBlock ?: return
        return execution.handler(errors.first())
    }

    when (event.data) {
        is IncomingMessage.Group -> dsl.groupExecutionBlock ?: dsl.executionBlock
        else -> dsl.privateExecutionBlock ?: dsl.executionBlock
    }?.invoke(execution)
}

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
