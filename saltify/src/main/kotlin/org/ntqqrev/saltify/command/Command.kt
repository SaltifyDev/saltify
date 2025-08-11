package org.ntqqrev.saltify.command

import io.github.oshai.kotlinlogging.KotlinLogging
import org.ntqqrev.saltify.dsl.CommandDslContext
import org.ntqqrev.saltify.dsl.CommandExecutionDslContext
import org.ntqqrev.saltify.dsl.CommonBuilder
import org.ntqqrev.saltify.dsl.ParamCapturer
import org.ntqqrev.saltify.event.FriendMessageReceiveEvent
import org.ntqqrev.saltify.event.GroupMessageReceiveEvent
import org.ntqqrev.saltify.event.MessageReceiveEvent
import org.ntqqrev.saltify.message.incoming.Segment
import org.ntqqrev.saltify.message.incoming.TextSegment
import org.ntqqrev.saltify.message.outgoing.GroupMessageBuilder
import org.ntqqrev.saltify.message.outgoing.PrivateMessageBuilder
import org.ntqqrev.saltify.model.Friend
import org.ntqqrev.saltify.model.GroupMember
import org.ntqqrev.saltify.model.User
import org.ntqqrev.saltify.plugin.Plugin
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

private val logger = KotlinLogging.logger { }

class Command(
    val name: String,
    val description: String,
    val plugin: Plugin<*>
) : CommandDslContext {
    internal var onPrivateExecuteBlock: (suspend CommandExecutionDslContext<Friend, PrivateMessageBuilder>.() -> Unit)? =
        null
    internal var onGroupExecuteBlock: (suspend CommandExecutionDslContext<GroupMember, GroupMessageBuilder>.() -> Unit)? =
        null

    internal val subCommands = mutableMapOf<String, Command>()

    internal var nodes: MutableList<CommandNode<*>>? = null
    internal var canHaveMoreNodes = true

    override fun subCommand(
        name: String, description: String, block: CommandDslContext.() -> Unit
    ) {
        if (subCommands.containsKey(name)) {
            throw IllegalArgumentException("Sub-command '$name' already exists in command '$this.name'")
        }
        val subCommand = Command(name, description, plugin).apply(block)
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
            else -> {
                if (type.isSubclassOf(Segment::class)) {
                    if (type == Segment::class) {
                        throw IllegalArgumentException(
                            "Cannot use Segment as a parameter type directly, " +
                                    "use specific segment types like ImageSegment."
                        )
                    }
                    if (type == TextSegment::class) {
                        throw IllegalArgumentException(
                            "TextSegment cannot be used as a segment node type. " +
                                    "Use string parameter<String> instead. " +
                                    "If you want to capture the whole text segment, " +
                                    "use greedyStringParameter."
                        )
                    }
                    SegmentNode(name, description, type as KClass<Segment>) as SegmentNode<T>
                }
                throw IllegalArgumentException(
                    "Unsupported parameter type: ${type.simpleName}. " +
                            "Supported types are Int, Long, Double, String, and Segment subclasses."
                )
            }
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
    override fun onExecute(block: suspend CommandExecutionDslContext<User, CommonBuilder>.() -> Unit) {
        onPrivateExecuteBlock =
            block as suspend CommandExecutionDslContext<Friend, PrivateMessageBuilder>.() -> Unit
        onGroupExecuteBlock =
            block as suspend CommandExecutionDslContext<GroupMember, GroupMessageBuilder>.() -> Unit
    }

    override fun onPrivateExecute(block: suspend CommandExecutionDslContext<Friend, PrivateMessageBuilder>.() -> Unit) {
        onPrivateExecuteBlock = block
    }

    override fun onGroupExecute(block: suspend CommandExecutionDslContext<GroupMember, GroupMessageBuilder>.() -> Unit) {
        onGroupExecuteBlock = block
    }

    suspend fun tryExecute(
        tokenizer: MessageTokenizer,
        event: MessageReceiveEvent
    ) {
        val message = event.message
        val first = tokenizer.read()
        if (first is TextToken) {
            val subCommand = subCommands[first.text]
            if (subCommand != null) {
                return subCommand.tryExecute(tokenizer, event)
            }
        }
        tokenizer.unread()
        if (nodes == null) {
            throw IllegalStateException("No parameters defined for command '$name'")
        }

        val captureContext = mutableMapOf<ParamCapturer<*>, Any>()
        for (node in nodes!!) {
            val current = if (node is StringNode && node.isGreedy) {
                tokenizer.remaining()
            } else {
                tokenizer.read()
            }
            val matchedValue = node.tryMatch(current) ?: throw IllegalArgumentException(
                "Failed to match parameter '${node.name}' with value '$current' in command '$name'"
            )
            captureContext[node] = matchedValue
        }
        when (event) {
            is FriendMessageReceiveEvent -> {
                if (onPrivateExecuteBlock == null) {
                    throw IllegalStateException(
                        "Invalid scope for command '$name': " +
                                "no private execution block defined"
                    )
                } else {
                    val execution = CommandExecution.Private(event, captureContext)
                    execution.apply {
                        onPrivateExecuteBlock!!.invoke(this)
                    }
                }
            }

            is GroupMessageReceiveEvent -> {
                if (onGroupExecuteBlock == null) {
                    throw IllegalStateException(
                        "Invalid scope for command '$name': " +
                                "no group execution block defined"
                    )
                } else {
                    val execution = CommandExecution.Group(event, captureContext)
                    execution.apply {
                        onGroupExecuteBlock!!.invoke(this)
                    }
                }
            }

            else -> {
                logger.warn {
                    "Invalid message type for command '$name': " +
                            "expected PrivateIncomingMessage or GroupIncomingMessage, got ${message::class.simpleName}"
                }
            }
        }
    }
}

