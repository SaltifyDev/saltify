package org.ntqqrev.saltify.command

/**
 * Defines an option's name.
 * It can only be used on a field of enum class.
 *
 * This annotation is not needed unless you need to specify a custom display name for the option.
 */
@Target(AnnotationTarget.FIELD)
annotation class OptionName(val name: String = "")
