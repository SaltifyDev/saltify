package org.ntqqrev.saltify.command

import org.ntqqrev.saltify.dsl.CommandDslContext
import org.ntqqrev.saltify.dsl.CommandExecutionDslContext
import org.ntqqrev.saltify.dsl.CommonBuilder
import org.ntqqrev.saltify.dsl.ParamCapturer
import org.ntqqrev.saltify.message.incoming.GroupIncomingMessage
import org.ntqqrev.saltify.message.incoming.IncomingMessage
import org.ntqqrev.saltify.message.incoming.PrivateIncomingMessage
import org.ntqqrev.saltify.message.outgoing.GroupMessageBuilder
import org.ntqqrev.saltify.message.outgoing.PrivateMessageBuilder
import kotlin.reflect.KClass

class Command(val name: String, val description: String) : CommandDslContext {
    internal var onPrivateExecuteBlock: (suspend CommandExecutionDslContext<PrivateIncomingMessage, PrivateMessageBuilder>.() -> Unit)? =
        null
    internal var onGroupExecuteBlock: (suspend CommandExecutionDslContext<GroupIncomingMessage, GroupMessageBuilder>.() -> Unit)? =
        null

    internal val subCommands = mutableMapOf<String, Command>()

    internal var nodes: MutableList<CommandNode<*>>? = null
    internal var canHaveMoreNodes = true

    override fun subCommand(
        name: String, description: String, block: CommandDslContext.() -> Unit
    ) {
        val subCommand = Command(name, description)
        block(subCommand)
        subCommands[name] = subCommand
    }

    private fun checkBeforeAddingNode() {
        if (nodes == null) {
            nodes = mutableListOf()
        }
        if (!canHaveMoreNodes) {
            throw IllegalStateException("Cannot add more parameters to this command.")
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> parameter(
        type: KClass<T>, name: String, description: String
    ): ParamCapturer<T> {
        checkBeforeAddingNode()
        val node: CommandNode<*> = when (type) {
            Int::class -> IntNode(name, description)
            Long::class -> LongNode(name, description)
            Double::class -> DoubleNode(name, description)
            String::class -> StringNode(name, description)
            else -> throw IllegalArgumentException("Unsupported parameter type: ${type.simpleName}")
        }
        nodes!!.add(node)
        return node as ParamCapturer<T>
    }

    override fun greedyStringParameter(
        name: String, description: String
    ): ParamCapturer<String> {
        checkBeforeAddingNode()
        val node = StringNode(name, description, isGreedy = true)
        nodes!!.add(node)
        return node
    }

    @Suppress("UNCHECKED_CAST")
    override fun onExecute(block: suspend CommandExecutionDslContext<IncomingMessage, CommonBuilder>.() -> Unit) {
        onPrivateExecuteBlock =
            block as suspend CommandExecutionDslContext<PrivateIncomingMessage, PrivateMessageBuilder>.() -> Unit
        onGroupExecuteBlock =
            block as suspend CommandExecutionDslContext<GroupIncomingMessage, GroupMessageBuilder>.() -> Unit
    }

    override fun onPrivateExecute(block: suspend CommandExecutionDslContext<PrivateIncomingMessage, PrivateMessageBuilder>.() -> Unit) {
        onPrivateExecuteBlock = block
    }

    override fun onGroupExecute(block: suspend CommandExecutionDslContext<GroupIncomingMessage, GroupMessageBuilder>.() -> Unit) {
        onGroupExecuteBlock = block
    }

    suspend fun tryExecute(
        tokenizer: Tokenizer,
        message: IncomingMessage
    ) {
        val first = tokenizer.read()
        val subCommand = subCommands[first]
        if (subCommand != null) {
            return subCommand.tryExecute(tokenizer, message)
        }
        if (nodes == null) {
            throw IllegalStateException("No parameters defined for command '$name'")
        }
        tokenizer.unread()

        val captureContext = mutableMapOf<ParamCapturer<*>, Any>()
        for (node in nodes!!) {
            val current = if (node is StringNode && node.isGreedy) {
                tokenizer.remaining()
            } else {
                tokenizer.read()
            }
            val matchedValue = node.tryMatch(current)
            if (matchedValue == null) {
                throw IllegalArgumentException(
                    "Failed to match parameter '${node.name}' with value '$current' in command '$name'"
                )
            }
            captureContext[node] = matchedValue
        }
        when (message) {
            is PrivateIncomingMessage -> {
                if (onPrivateExecuteBlock == null) {
                    throw IllegalStateException("Invalid scope for command '$name': " +
                            "no private execution block defined")
                } else {
                    val execution = CommandExecution.Private(message, captureContext)
                    execution.apply {
                        onPrivateExecuteBlock!!.invoke(this)
                    }
                }
            }
            is GroupIncomingMessage -> {
                if (onGroupExecuteBlock == null) {
                    throw IllegalStateException("Invalid scope for command '$name': " +
                            "no group execution block defined")
                } else {
                    val execution = CommandExecution.Group(message, captureContext)
                    execution.apply {
                        onGroupExecuteBlock!!.invoke(this)
                    }
                }
            }
            else -> {
                throw IllegalStateException("Invalid message type for command '$name': " +
                        "expected PrivateIncomingMessage or GroupIncomingMessage, got ${message::class.simpleName}")
            }
        }
    }
}

