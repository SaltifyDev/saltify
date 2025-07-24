package org.ntqqrev.saltify.command

import org.ntqqrev.saltify.message.incoming.IncomingMessage
import org.ntqqrev.saltify.message.incoming.TextSegment

private const val segmentTokenPrefix = 0x1F.toChar()

class MessageTokenizer(
    private val message: IncomingMessage,
    startIndex: Int = 0
) : ITokenizer<Token> {
    private val textTokenizer = TextTokenizer(
        message.segments
            .drop(startIndex)
            .mapIndexed { index, segment ->
                when (segment) {
                    is TextSegment -> segment.text
                    else -> "$segmentTokenPrefix${index + startIndex}"
                }
            }
            .joinToString(" ")
    )

    override fun hasMoreTokens() = textTokenizer.hasMoreTokens()

    override fun read(): Token {
        val rawToken = textTokenizer.read()
        return when {
            rawToken.startsWith(segmentTokenPrefix) -> {
                val index = rawToken.substring(1).toInt()
                SegmentToken(message.segments[index])
            }
            else -> TextToken(rawToken)
        }
    }

    override fun unread() {
        textTokenizer.unread()
    }

    override fun remaining(): Token {
        val rawToken = textTokenizer.remaining()
        if (segmentTokenPrefix in rawToken) {
            throw IllegalStateException(
                "Cannot read remaining token when it contains segments"
            )
        }
        return TextToken(rawToken)
    }
}