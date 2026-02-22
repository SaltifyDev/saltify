package org.ntqqrev.saltify.model

import org.ntqqrev.saltify.dsl.SaltifyCommandParamDef

/**
 * 指令解析错误
 */
public sealed class CommandError {
    public abstract val message: String

    public data class MissingParam(val parameter: SaltifyCommandParamDef<*>) : CommandError() {
        override val message: String
            get() = "Missing required parameter: ${parameter.name}"
    }

    public data class InvalidParam(val parameter: SaltifyCommandParamDef<*>, val token: String) : CommandError() {
        override val message: String
            get() = "Invalid argument provided for parameter ${parameter.name}: \"$token\""
    }

    public data class TooManyArguments(val extraTokens: List<String>) : CommandError() {
        override val message: String
            get() = "Too many arguments provided: ${extraTokens.joinToString(" ")}"
    }
}
