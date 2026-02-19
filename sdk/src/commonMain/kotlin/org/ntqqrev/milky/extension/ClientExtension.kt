package org.ntqqrev.milky.extension

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.ntqqrev.milky.Event
import org.ntqqrev.milky.IncomingMessage
import org.ntqqrev.milky.IncomingSegment
import org.ntqqrev.milky.core.MilkyClient
import org.ntqqrev.milky.dsl.MilkyCommandDsl
import org.ntqqrev.milky.dsl.MilkyCommandExecution
import org.ntqqrev.milky.dsl.MilkyParamCapturer
import kotlin.reflect.KClass

public inline fun <reified T : Event> MilkyClient.on(
    crossinline block: suspend MilkyClient.(event: T) -> Unit
): Job = clientScope.launch {
    eventFlow.filter { it is T }.collect { block(it as T) }
}

public fun MilkyClient.command(
    name: String,
    prefix: String = "/",
    builder: MilkyCommandDsl.() -> Unit
) {
    val rootDsl = MilkyCommandDsl().apply(builder)

    on<Event.MessageReceive> { event ->
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
    dsl: MilkyCommandDsl,
    tokens: List<String>,
    client: MilkyClient,
    event: Event.MessageReceive
) {
    if (tokens.isNotEmpty()) {
        val subName = tokens[0]
        val subCommand = dsl.subCommands.find { it.first == subName }
        if (subCommand != null) {
            val subDsl = MilkyCommandDsl().apply(subCommand.second)
            executeCommand(subDsl, tokens.drop(1), client, event)
            return
        }
    }

    val argumentMap = mutableMapOf<MilkyParamCapturer<*>, Any?>()
    var currentTokens = tokens

    for (param in dsl.parameters) {
        if (currentTokens.isEmpty()) {
            argumentMap[param] = null
            continue
        }

        if (param.isGreedy) {
            argumentMap[param] = currentTokens.joinToString(" ")
            currentTokens = emptyList()
        } else {
            val rawValue = currentTokens[0]
            argumentMap[param] = convertValue(rawValue, param.type)
            currentTokens = currentTokens.drop(1)
        }
    }

    val execution = MilkyCommandExecution(client, event, argumentMap)
    val block = when (event.data) {
        is IncomingMessage.Group -> dsl.groupExecutionBlock ?: dsl.executionBlock
        else -> dsl.privateExecutionBlock ?: dsl.executionBlock
    }

    block?.invoke(execution)
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
