package org.ntqqrev.saltify.config

/**
 * Represents an enumeration of options that can be configured.
 * The enumeration of the configurable item should implement this interface.
 */
@Target(AnnotationTarget.FIELD)
annotation class Option(val description: String)