package org.ntqqrev.saltify.command

import org.ntqqrev.saltify.message.incoming.Segment

sealed class Token

class TextToken(val text: String) : Token()

class SegmentToken(val segment: Segment) : Token()