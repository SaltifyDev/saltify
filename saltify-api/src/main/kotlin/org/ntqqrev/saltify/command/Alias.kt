package org.ntqqrev.saltify.command

/**
 * Defines an alias for a command or subcommand.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
annotation class Alias(vararg val aliases: String)
