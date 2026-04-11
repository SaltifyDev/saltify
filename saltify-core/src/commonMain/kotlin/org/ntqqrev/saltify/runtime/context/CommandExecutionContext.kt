package org.ntqqrev.saltify.runtime.context

import io.ktor.util.logging.*
import org.ntqqrev.milky.Event
import org.ntqqrev.saltify.SaltifyApplication
import org.ntqqrev.saltify.model.command.ParameterParseResult
import org.ntqqrev.saltify.runtime.command.CommandParameter

public open class CommandExecutionContext(
    public override val client: SaltifyApplication,
    public override val event: Event.MessageReceive,
    private val argumentMap: Map<CommandParameter<*>, Any?>,
    commandName: String,
) : EventContext<Event.MessageReceive>(event, client) {
    public val logger: Logger = KtorSimpleLogger("Saltify/cmd:$commandName")

    /**
     * 获取已解析的参数值。
     */
    @Suppress("UNCHECKED_CAST")
    public val <T : Any> CommandParameter<T>.value: T
        get() = (argumentMap[this] as? ParameterParseResult.Success<T>)?.value!!
}
