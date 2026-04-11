package org.ntqqrev.saltify.model.command

import org.ntqqrev.saltify.runtime.command.CommandParameter

/**
 * 指令解析错误
 */
public sealed class CommandError {
    public abstract val message: String

    public data class MissingParam(val parameter: CommandParameter<*>) : CommandError() {
        override val message: String
            get() = "缺少必要的参数: ${parameter.name}"
    }

    public data class InvalidParam(val parameter: CommandParameter<*>, val token: String) : CommandError() {
        override val message: String
            get() = "参数 ${parameter.name} 无效: \"$token\""
    }

    public data class TooManyArguments(val extraTokens: List<String>) : CommandError() {
        override val message: String
            get() = "提供参数过多: ${extraTokens.joinToString(" ")}"
    }
}
