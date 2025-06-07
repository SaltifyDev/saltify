package org.ntqqrev.saltify.config

/**
 * Describes an option that can be configured.
 */
@Target(AnnotationTarget.FIELD)
annotation class Configurable(val name: String, val description: String = "")