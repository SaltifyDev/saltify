package org.ntqqrev.saltify.command

import org.ntqqrev.saltify.message.incoming.Segment
import org.ntqqrev.saltify.message.incoming.TextSegment

sealed class Token

class TextToken(val text: String) : Token()

class SegmentToken(val segment: Segment) : Token()