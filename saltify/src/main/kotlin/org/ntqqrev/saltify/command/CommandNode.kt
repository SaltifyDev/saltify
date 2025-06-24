package org.ntqqrev.saltify.command

import org.ntqqrev.saltify.dsl.ParamCapturer
import org.ntqqrev.saltify.message.incoming.Segment
import kotlin.reflect.KClass

sealed class CommandNode<T>(
    val name: String,
    val description: String,
) : ParamCapturer<T> {
    abstract fun tryMatch(token: Token): T?
}

class IntNode(name: String, description: String) : CommandNode<Int>(name, description) {
    override fun tryMatch(token: Token): Int? {
        return (token as? TextToken)?.text?.toIntOrNull()
    }
}

class LongNode(name: String, description: String) : CommandNode<Long>(name, description) {
    override fun tryMatch(token: Token): Long? {
        return (token as? TextToken)?.text?.toLongOrNull()
    }
}

class DoubleNode(name: String, description: String) : CommandNode<Double>(name, description) {
    override fun tryMatch(token: Token): Double? {
        return (token as? TextToken)?.text?.toDoubleOrNull()
    }
}

class StringNode(
    name: String,
    description: String,
    val isGreedy: Boolean = false
) : CommandNode<String>(name, description) {
    override fun tryMatch(token: Token): String? {
        return (token as? TextToken)?.text
    }
}

class SegmentNode<T : Segment>(
    name: String,
    description: String,
    val segmentClass: KClass<T>
) : CommandNode<T>(name, description) {

    override fun tryMatch(token: Token): T? {
        return if (token is SegmentToken && segmentClass.isInstance(token.segment)) {
            @Suppress("UNCHECKED_CAST")
            token.segment as T
        } else {
            null
        }
    }
}