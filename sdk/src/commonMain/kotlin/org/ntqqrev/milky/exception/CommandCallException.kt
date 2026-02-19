package org.ntqqrev.milky.exception

import org.ntqqrev.milky.dsl.MilkyParamCapturer

public sealed class CommandCallException(message: String) : IllegalStateException(message), MilkyException {
    public class ParameterMissing(public val parameter: MilkyParamCapturer<*>) :
        CommandCallException("Missing required parameter: ${parameter.name}")

    public class ParameterInvalidType(public val parameter: MilkyParamCapturer<*>, public val value: String) :
        CommandCallException(
            "Invalid type for parameter ${parameter.name}: '$value' cannot be converted to ${parameter.type.simpleName}"
        )
}
