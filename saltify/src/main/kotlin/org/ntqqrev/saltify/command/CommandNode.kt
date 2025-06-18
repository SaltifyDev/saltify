package org.ntqqrev.saltify.command

import org.ntqqrev.saltify.dsl.ParamCapturer

sealed class CommandNode<T>(
    val name: String,
    val description: String,
) : ParamCapturer<T> {
    abstract fun tryMatch(token: String): T?
}

class IntNode(name: String, description: String) : CommandNode<Int>(name, description) {
    override fun tryMatch(token: String): Int? {
        return token.toIntOrNull()
    }
}

class LongNode(name: String, description: String) : CommandNode<Long>(name, description) {
    override fun tryMatch(token: String): Long? {
        return token.toLongOrNull()
    }
}

class DoubleNode(name: String, description: String) : CommandNode<Double>(name, description) {
    override fun tryMatch(token: String): Double? {
        return token.toDoubleOrNull()
    }
}

class StringNode(
    name: String,
    description: String,
    val isGreedy: Boolean = false
) : CommandNode<String>(name, description) {
    override fun tryMatch(token: String): String? {
        return token
    }
}