package org.ntqqrev.saltify.command

import org.ntqqrev.saltify.message.incoming.Segment
import org.ntqqrev.saltify.message.incoming.TextSegment

sealed class Token

class TextToken(val text: String) : Token()

class SegmentToken(val segment: Segment) : Token() {
    init {
        if (segment is TextSegment) {
            throw IllegalArgumentException(
                "TextSegment cannot be used as a SegmentToken. Use TextToken instead."
            )
        }
    }
}