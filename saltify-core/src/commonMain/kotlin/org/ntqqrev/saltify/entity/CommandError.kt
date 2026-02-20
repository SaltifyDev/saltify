package org.ntqqrev.saltify.entity

import org.ntqqrev.saltify.dsl.SaltifyCommandParamDef

public sealed class CommandError {
    public data class MissingParam(val parameter: SaltifyCommandParamDef<*>) : CommandError()
    public data class InvalidParam(val parameter: SaltifyCommandParamDef<*>, val value: String) : CommandError()
}
