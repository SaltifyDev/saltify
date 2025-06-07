package org.ntqqrev.saltify.command

/**
 * Defines a command.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
annotation class Command(val name: String = "")
