package org.ntqqrev.saltify.command

@Target(AnnotationTarget.FUNCTION)
annotation class Subcommand(val name: String = "")
