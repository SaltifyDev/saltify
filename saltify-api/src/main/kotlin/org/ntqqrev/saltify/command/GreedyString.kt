package org.ntqqrev.saltify.command

/**
 * Defines a parameter that captures the rest of the command line as a single string.
 *
 * This annotation is used to indicate that the parameter should capture all remaining input
 * after the specified parameters, allowing for flexible command parsing.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class GreedyString()
