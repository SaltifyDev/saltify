package org.ntqqrev.saltify.model.command

import org.ntqqrev.milky.IncomingSegment
import org.ntqqrev.saltify.extension.plainText
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

    public data class InvalidParam(val parameter: CommandParameter<*>, val segment: IncomingSegment) : CommandError() {
        override val message: String
            get() = "参数 ${parameter.name} 无效: \"${segment.plainText}\""
    }

    public data class TooManyArguments(val extraSegments: List<IncomingSegment>) : CommandError() {
        override val message: String
            get() = "提供参数过多: ${extraSegments.joinToString(" ") { it.plainText }}"
    }
}
