package org.ntqqrev.saltify.dsl

import org.ntqqrev.saltify.Entity
import org.ntqqrev.saltify.message.incoming.GroupIncomingMessage
import org.ntqqrev.saltify.message.incoming.IncomingMessage
import org.ntqqrev.saltify.message.incoming.PrivateIncomingMessage
import org.ntqqrev.saltify.message.outgoing.*
import kotlin.reflect.KClass

interface CommandDslContext {
    /**
     * Defines a sub command with the given [name] and [description].
     */
    fun subCommand(
        name: String, description: String = "", block: CommandDslContext.() -> Unit
    )

    /**
     * Defines a parameter with the given [type], [name] and [description].
     * @return a [ParamCapturer] that can be used to capture the parameter value
     * when the command is executed.
     */
    fun <T : Any> parameter(
        type: KClass<T>,
        name: String,
        description: String = "",
    ): ParamCapturer<T>

    /**
     * Defines an option with the given [name] and [description].
     * When the command is executed, the option value will be captured
     * greedily, meaning it will capture all remaining text.
     * @return a [ParamCapturer] that can be used to capture the option value
     * when the command is executed.
     */
    fun greedyStringParameter(
        name: String, description: String = ""
    ): ParamCapturer<String>

    /**
     * Executes the given [block] when the command is executed in any context.
     */
    fun onExecute(
        block: suspend CommandExecutionDslContext<IncomingMessage, CommonBuilder>.() -> Unit
    )

    /**
     * Executes the given [block] when the command is executed in a private chat.
     */
    fun onPrivateExecute(
        block: suspend CommandExecutionDslContext<PrivateIncomingMessage, PrivateMessageBuilder>.() -> Unit
    )

    /**
     * Executes the given [block] when the command is executed in a group.
     */
    fun onGroupExecute(
        block: suspend CommandExecutionDslContext<GroupIncomingMessage, GroupMessageBuilder>.() -> Unit
    )
}

inline fun <reified T : Any> CommandDslContext.parameter(
    name: String, description: String = ""
): ParamCapturer<T> {
    return parameter(T::class, name, description)
}

interface CommandExecutionDslContext<M : IncomingMessage, B : Entity> {
    /**
     * The raw message that triggered the command execution.
     */
    val message: M

    /**
     * Captures a parameter of the specified type [T] using the provided [capturer]
     * when the command is executed.
     */
    fun <T : Any> capture(capturer: ParamCapturer<T>): T

    /**
     * Builds a message using the provided [block] to send a response.
     */
    suspend fun respond(block: B.() -> Unit)
}

interface CommonBuilder :
    Entity,
    TextFeature,
    FaceFeature,
    ReplyFeature,
    ImageFeature,
    RecordFeature,
    VideoFeature,
    ForwardFeature

interface ParamCapturer<T>