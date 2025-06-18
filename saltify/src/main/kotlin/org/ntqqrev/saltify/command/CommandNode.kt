package org.ntqqrev.saltify.command

sealed class CommandNode<T>(
    val parent: CommandNode<*>?
) {
    internal val children: MutableList<CommandNode<*>> = mutableListOf()

    abstract fun tryMatch(token: String): T?
}

class LiteralNode(
    parent: CommandNode<*>?,
    private val literal: String
) : CommandNode<String>(parent) {
    override fun tryMatch(token: String): String? {
        return if (token.equals(literal, ignoreCase = true)) literal else null
    }
}

class IntNode(
    parent: CommandNode<*>?
) : CommandNode<Int>(parent) {
    override fun tryMatch(token: String): Int? {
        return token.toIntOrNull()
    }
}

class LongNode(
    parent: CommandNode<*>?
) : CommandNode<Long>(parent) {
    override fun tryMatch(token: String): Long? {
        return token.toLongOrNull()
    }
}

class DoubleNode(
    parent: CommandNode<*>?
) : CommandNode<Double>(parent) {
    override fun tryMatch(token: String): Double? {
        return token.toDoubleOrNull()
    }
}

class StringNode(
    parent: CommandNode<*>?,
    val isGreedy: Boolean = false
) : CommandNode<String>(parent) {
    override fun tryMatch(token: String): String? {
        return token
    }
}