package org.ntqqrev.saltify.model.command

import org.ntqqrev.milky.IncomingSegment

internal sealed class ParameterParseResult<out T : Any> {
    data class Success<T : Any>(val value: T) : ParameterParseResult<T>()
    data class InvalidParam(val rawValue: IncomingSegment) : ParameterParseResult<Unit>()
    object MissingParam : ParameterParseResult<Unit>()
}
