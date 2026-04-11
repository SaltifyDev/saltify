package org.ntqqrev.saltify.runtime.command

/**
 * 已注册指令的信息
 */
public data class RegisteredCommand(
    val name: String,
    val prefix: String,
    val description: String,
    val parameters: List<CommandParameter<*>>,
    val subCommands: List<RegisteredSubCommand>,
    val pluginName: String?
)

/**
 * 已注册子指令的信息
 */
public data class RegisteredSubCommand(
    val name: String,
    val description: String,
    val parameters: List<CommandParameter<*>>,
    val subCommands: List<RegisteredSubCommand> = emptyList()
)
