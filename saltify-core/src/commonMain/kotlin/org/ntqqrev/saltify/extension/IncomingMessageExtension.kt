package org.ntqqrev.saltify.extension

import org.ntqqrev.milky.IncomingSegment
import org.ntqqrev.saltify.core.text

public val List<IncomingSegment>.plainText: String?
    get() = filterIsInstance<IncomingSegment.Text>()
        .map { it.text }
        .takeIf { it.isNotEmpty() }
        ?.joinToString("")
