package org.ntqqrev.saltify.command

import org.ntqqrev.saltify.Entity
import org.ntqqrev.saltify.dsl.CommandExecutionDslContext
import org.ntqqrev.saltify.dsl.ParamCapturer
import org.ntqqrev.saltify.message.incoming.GroupIncomingMessage
import org.ntqqrev.saltify.message.incoming.IncomingMessage
import org.ntqqrev.saltify.message.incoming.PrivateIncomingMessage
import org.ntqqrev.saltify.message.outgoing.GroupMessageBuilder
import org.ntqqrev.saltify.message.outgoing.PrivateMessageBuilder
import org.ntqqrev.saltify.model.Friend
import org.ntqqrev.saltify.sendMessage

abstract class CommandExecution<M : IncomingMessage, B : Entity>(
    override val message: M,
    val captureContext: Map<ParamCapturer<*>, Any>
) : CommandExecutionDslContext<M, B> {
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> capture(capturer: ParamCapturer<T>): T =
        captureContext[capturer] as? T
            ?: throw IllegalArgumentException(
                "Unexpectedly no value captured for some capture node"
            )

    class Private(
        override val message: PrivateIncomingMessage,
        captureContext: Map<ParamCapturer<*>, Any>
    ) : CommandExecution<PrivateIncomingMessage, PrivateMessageBuilder>(message, captureContext) {
        override suspend fun respond(block: PrivateMessageBuilder.() -> Unit) {
            val friend = message.peer as? Friend
                ?: throw IllegalStateException("Cannot send message to non-friend peer: ${message.peer}")
            friend.sendMessage(block)
        }
    }

    class Group(
        override val message: GroupIncomingMessage,
        captureContext: Map<ParamCapturer<*>, Any>
    ) : CommandExecution<GroupIncomingMessage, GroupMessageBuilder>(message, captureContext) {
        override suspend fun respond(block: GroupMessageBuilder.() -> Unit) {
            message.group.sendMessage(block)
        }
    }
}