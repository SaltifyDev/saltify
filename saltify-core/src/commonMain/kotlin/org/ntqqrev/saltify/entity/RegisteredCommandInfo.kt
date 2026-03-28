package org.ntqqrev.saltify.entity

import org.ntqqrev.saltify.dsl.SaltifyCommandParamDef

/**
 * 子指令的注册信息
 */
public data class RegisteredSubCommandInfo(
    val name: String,
    val description: String,
    val parameters: List<SaltifyCommandParamDef<*>>,
    val subCommands: List<RegisteredSubCommandInfo> = emptyList()
)

/**
 * 已注册指令的信息
 */
public data class RegisteredCommandInfo(
    val name: String,
    val prefix: String,
    val description: String,
    val parameters: List<SaltifyCommandParamDef<*>>,
    val subCommands: List<RegisteredSubCommandInfo>,
    val pluginName: String?
)
