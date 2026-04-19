@file:Suppress("LongParameterList", "ReturnCount")

package org.ntqqrev.saltify.internal.engine

import org.ntqqrev.milky.Event
import org.ntqqrev.milky.IncomingMessage
import org.ntqqrev.milky.IncomingSegment
import org.ntqqrev.saltify.SaltifyApplication
import org.ntqqrev.saltify.dsl.CommandBuilder
import org.ntqqrev.saltify.model.command.CommandError
import org.ntqqrev.saltify.model.command.ParameterParseResult
import org.ntqqrev.saltify.runtime.command.CommandParameter
import org.ntqqrev.saltify.runtime.command.CommandRequirementMatch
import org.ntqqrev.saltify.runtime.context.CommandExecutionContext
import kotlin.time.Clock

internal object CommandEngine {
    suspend fun execute(
        dsl: CommandBuilder,
        segments: List<IncomingSegment>,
        client: SaltifyApplication,
        event: Event.MessageReceive,
        name: String
    ) {
        val argumentMap = mutableMapOf<CommandParameter<*>, ParameterParseResult<Any>>()
        val execution = CommandExecutionContext(client, event, argumentMap, name)

        if (!checkRequirements(dsl, execution)) return
        if (segments.isNotEmpty()) {
            val subName = (segments[0] as? IncomingSegment.Text)?.text
            val subCommand = dsl.subCommands.find { it.first == subName }

            if (subCommand != null) return execute(
                subCommand.second,
                segments.drop(1),
                client,
                event,
                "$name $subName"
            )
        }

        val error = parseParameters(dsl, segments, argumentMap)
        if (error != null) {
            dsl.failureBlock?.invoke(execution, error)
            return
        }

        dispatch(dsl, execution, name)
    }

    private fun checkRequirements(dsl: CommandBuilder, execution: CommandExecutionContext): Boolean {
        return dsl.requirementBlock?.let { block ->
            CommandRequirementMatch(execution).block().satisfies()
        } ?: true
    }

    /**
     * 返回第一个遇到的 [CommandError]，若全部成功则返回 null。
     */
    private fun parseParameters(
        dsl: CommandBuilder,
        segments: List<IncomingSegment>,
        argumentMap: MutableMap<CommandParameter<*>, ParameterParseResult<Any>>
    ): CommandError? {
        val currentTokens = segments.toMutableList()

        for (param in dsl.parameters) {
            val result: ParameterParseResult<Any> = when {
                currentTokens.isEmpty() -> ParameterParseResult.MissingParam
                param.isGreedy -> {
                    val greedyValue = currentTokens.joinToString(" ") { segment ->
                        if (segment is IncomingSegment.Text) {
                            segment.text
                        } else {
                            segment.toString()
                        }
                    }

                    currentTokens.clear()
                    ParameterParseResult.Success(greedyValue)
                }
                else -> {
                    val rawValue = currentTokens.removeFirst()

                    param.transform(rawValue)?.let { ParameterParseResult.Success(it) }
                        ?: ParameterParseResult.InvalidParam(rawValue)
                }
            }

            argumentMap[param] = result
            return when (result) {
                is ParameterParseResult.MissingParam -> CommandError.MissingParam(param)
                is ParameterParseResult.InvalidParam -> CommandError.InvalidParam(param, result.rawValue)
                else -> continue
            }
        }

        if (currentTokens.isNotEmpty()) return CommandError.TooManyArguments(currentTokens)

        return null
    }

    private suspend fun dispatch(dsl: CommandBuilder, execution: CommandExecutionContext, name: String) {
        val startInstant = Clock.System.now()
        execution.logger.info("${execution.event.peerId} 触发了 $name 指令 (seq=${execution.event.messageSeq})")

        when (execution.event.data) {
            is IncomingMessage.Group -> dsl.groupExecutionBlock ?: dsl.executionBlock
            else -> dsl.privateExecutionBlock ?: dsl.executionBlock
        }?.invoke(execution)

        execution.logger.info("seq=${execution.event.messageSeq} 处理完成, 用时 ${Clock.System.now() - startInstant}")
    }
}
