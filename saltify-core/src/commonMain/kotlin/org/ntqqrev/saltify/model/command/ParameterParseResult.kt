package org.ntqqrev.saltify.model.command

internal sealed class ParameterParseResult<out T : Any> {
    data class Success<T : Any>(val value: T) : ParameterParseResult<T>()
    data class InvalidParam(val rawValue: String) : ParameterParseResult<Unit>()
    object MissingParam : ParameterParseResult<Unit>()
}
