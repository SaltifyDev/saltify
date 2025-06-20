package org.ntqqrev.saltify.command

import org.ntqqrev.saltify.message.incoming.IncomingMessage
import org.ntqqrev.saltify.message.incoming.TextSegment

class MessageTokenizer(
    private val message: IncomingMessage,
    startIndex: Int = 0
) : ITokenizer<Token> {
    private var index = startIndex
    private var currentTextTokenizer: TextTokenizer? = null
    private var textHasBeenRead = false

    override fun hasMoreTokens(): Boolean =
        index < message.segments.size || (currentTextTokenizer?.hasMoreTokens() ?: false)

    override fun read(): Token {
        if (currentTextTokenizer != null) {
            textHasBeenRead = true
            if (currentTextTokenizer!!.hasMoreTokens()) {
                return TextToken(currentTextTokenizer!!.read())
            } else {
                currentTextTokenizer = null
                index++
                return read()
            }
        }
        val segment = message.segments[index]
        if (segment is TextSegment) {
            currentTextTokenizer = TextTokenizer(segment.text)
            textHasBeenRead = false
            return read()
        } else {
            index++
            return SegmentToken(segment)
        }
    }

    override fun unread() {
        if (currentTextTokenizer != null) {
            if (textHasBeenRead) {
                currentTextTokenizer!!.unread()
                return
            } else {
                currentTextTokenizer = null
            }
        }
        if (index == 0)
            throw IllegalStateException("Cannot unread beyond the beginning of segments.")
        index--
    }

    override fun remaining(): Token {
        if (index < message.segments.size || currentTextTokenizer == null) {
            throw IllegalStateException(
                "Cannot read remaining token when there are segments other than text segments left."
            )
        }
        if (currentTextTokenizer!!.hasMoreTokens()) {
            return TextToken(currentTextTokenizer!!.remaining())
        } else {
            throw NoSuchElementException("No more tokens available in the message.")
        }
    }
}