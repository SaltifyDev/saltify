package org.ntqqrev.saltify.extension

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filterIsInstance
import org.ntqqrev.milky.Event
import org.ntqqrev.milky.IncomingSegment
import org.ntqqrev.saltify.dsl.CommandBuilder
import org.ntqqrev.saltify.internal.engine.CommandEngine
import org.ntqqrev.saltify.model.SaltifyComponentType
import org.ntqqrev.saltify.runtime.command.RegisteredCommand
import org.ntqqrev.saltify.runtime.command.RegisteredSubCommand
import org.ntqqrev.saltify.runtime.context.ApplicationContext
import org.ntqqrev.saltify.runtime.context.EventContext
import org.ntqqrev.saltify.runtime.saltifyComponent

/**
 * 注册一个事件监听器。
 */
public inline fun <reified T : Event> ApplicationContext.on(
    crossinline block: suspend EventContext<T>.() -> Unit
): Job = (this as? CoroutineScope ?: client.extensionScope).launch {
    client.eventFlow
        .filterIsInstance<T>()
        .collect {
            launch {
                runCatching {
                    EventContext(it, client).block()
                }.onFailure { throwable ->
                    if (throwable is CancellationException) throw throwable

                    client.exceptionHandlerProvider.exceptionFlow.tryEmit(
                        currentCoroutineContext() to throwable
                    )
                }
            }
        }
}

/**
 * 注册一个消息正则匹配监听器。
 */
public inline fun ApplicationContext.regex(
    regex: String,
    crossinline block: suspend EventContext<Event.MessageReceive>.(matches: Sequence<MatchResult>) -> Unit
): Job {
    val regex = Regex(regex)

    return on<Event.MessageReceive> {
        val text = event.segments.filterIsInstance<IncomingSegment.Text>()
            .joinToString("") { it.text }

        val matches = regex.findAll(text)
        if (matches.any()) block(matches)
    }
}

private val spaceOrWordRegex = Regex("([^ ]+| +)")

/**
 * 注册一个指令。
 */
public fun ApplicationContext.command(
    name: String,
    prefix: String = client.config.bot.commandPrefix,
    builder: CommandBuilder.() -> Unit
): Job {
    val scope = this as? CoroutineScope ?: client.extensionScope
    val rootDsl = CommandBuilder().apply(builder)

    val component = scope.coroutineContext.saltifyComponent!!
    val pluginName = if (component.type == SaltifyComponentType.Plugin) component.name else null

    client.commandRegistry.add(
        RegisteredCommand(
            name,
            prefix,
            rootDsl.description,
            rootDsl.parameters.toList(),
            rootDsl.subCommands.map { (subName, subCtx) ->
                subCtx.toSubCommand(subName)
            },
            pluginName
        )
    )

    return on<Event.MessageReceive> {
        val segments = event.segments.flatMap { segment ->
            if (segment is IncomingSegment.Text) {
                spaceOrWordRegex.findAll(segment.text)
                    .mapNotNull { match ->
                        val str = match.value

                        val processedStr = if (str.startsWith(" ")) str.drop(1) else str
                        if (processedStr.isNotEmpty()) {
                            IncomingSegment.Text(IncomingSegment.Text.Data(processedStr))
                        } else {
                            null
                        }
                    }.toList()
            } else {
                listOf(segment)
            }
        }
        val leadingText = (segments[0] as? IncomingSegment.Text)?.text ?: return@on

        if (!leadingText.startsWith("$prefix$name")) return@on

        CommandEngine.execute(
            rootDsl,
            segments.drop(1),
            client,
            event,
            name
        )
    }
}

private fun CommandBuilder.toSubCommand(name: String): RegisteredSubCommand =
    RegisteredSubCommand(
        name = name,
        description = description,
        parameters = parameters.toList(),
        subCommands = subCommands.map { (subName, subCtx) -> subCtx.toSubCommand(subName) }
    )
