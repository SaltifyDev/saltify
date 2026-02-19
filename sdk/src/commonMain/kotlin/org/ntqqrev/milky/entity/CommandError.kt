package org.ntqqrev.milky.entity

import org.ntqqrev.milky.dsl.MilkyParamCapturer

public sealed class CommandError {
    public data class MissingParam(val parameter: MilkyParamCapturer<*>) : CommandError()
    public data class InvalidParam(val parameter: MilkyParamCapturer<*>, val value: String) : CommandError()
}
