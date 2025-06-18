package org.ntqqrev.saltify.command

import java.util.Stack

class Tokenizer(private val input: String) {
    private var position: Int = 0
    private val length: Int = input.length
    private val positionStack = Stack<Int>()

    fun hasMoreTokens(): Boolean {
        skipWhitespace()
        return position < length
    }

    fun read(): String {
        skipWhitespace()
        if (!hasMoreTokens()) return ""

        positionStack.push(position)

        return when (val currentChar = input[position]) {
            '"' -> readQuotedToken(currentChar)
            else -> readNormalToken()
        }
    }

    fun unread() {
        if (positionStack.isNotEmpty()) {
            position = positionStack.pop()
        }
    }

    fun remaining(): String {
        skipWhitespace()
        positionStack.push(position)
        val remaining = input.substring(position)
        position = length
        return remaining
    }

    private fun readQuotedToken(quoteChar: Char): String {
        position++
        val sb = StringBuilder()
        var escapeNext = false

        while (position < length) {
            val current = input[position]

            when {
                escapeNext -> {
                    sb.append(current)
                    escapeNext = false
                    position++
                }
                current == '\\' -> {
                    escapeNext = true
                    position++
                }
                current == quoteChar -> {
                    position++
                    return sb.toString()
                }
                else -> {
                    sb.append(current)
                    position++
                }
            }
        }

        // The quoted token did not end with a closing quote
        throw IllegalArgumentException(
            "Unterminated quoted token starting from ${input.substring(position)}"
        )
    }

    private fun readNormalToken(): String {
        val start = position
        while (position < length && !input[position].isWhitespace()) {
            position++
        }
        return input.substring(start, position)
    }

    private fun skipWhitespace() {
        while (position < length && input[position].isWhitespace()) {
            position++
        }
    }
}