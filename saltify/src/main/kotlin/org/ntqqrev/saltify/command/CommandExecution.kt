package org.ntqqrev.saltify.command

import org.ntqqrev.saltify.Entity
import org.ntqqrev.saltify.dsl.CommandExecutionDslContext
import org.ntqqrev.saltify.dsl.ParamCapturer
import org.ntqqrev.saltify.event.FriendMessageReceiveEvent
import org.ntqqrev.saltify.event.GroupMessageReceiveEvent
import org.ntqqrev.saltify.message.incoming.IncomingMessage
import org.ntqqrev.saltify.message.outgoing.GroupMessageBuilder
import org.ntqqrev.saltify.message.outgoing.PrivateMessageBuilder
import org.ntqqrev.saltify.model.Friend
import org.ntqqrev.saltify.model.GroupMember
import org.ntqqrev.saltify.model.User
import org.ntqqrev.saltify.sendMessage

abstract class CommandExecution<U : User, B : Entity>(
    override val sender: U,
    override val message: IncomingMessage,
    val captureContext: Map<ParamCapturer<*>, Any>
) : CommandExecutionDslContext<U, B> {
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> capture(capturer: ParamCapturer<T>): T =
        captureContext[capturer] as? T
            ?: throw IllegalArgumentException(
                "Unexpectedly no value captured for some capture node"
            )

    class Private(
        event: FriendMessageReceiveEvent,
        captureContext: Map<ParamCapturer<*>, Any>
    ) : CommandExecution<Friend, PrivateMessageBuilder>(event.friend, event.message, captureContext) {
        override suspend fun respond(block: PrivateMessageBuilder.() -> Unit) {
            sender.sendMessage(block)
        }
    }

    class Group(
        event: GroupMessageReceiveEvent,
        captureContext: Map<ParamCapturer<*>, Any>
    ) : CommandExecution<GroupMember, GroupMessageBuilder>(event.sender, event.message, captureContext) {
        override suspend fun respond(block: GroupMessageBuilder.() -> Unit) {
            sender.group.sendMessage(block)
        }
    }
}