package org.ntqqrev.saltify.command

/**
 * Defines a parameter's name.
 *
 * This annotation is not needed unless you need to specify a custom display name for the parameter.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class ParamName(val name: String)
