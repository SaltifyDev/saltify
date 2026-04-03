package org.ntqqrev.saltify.builtin.plugin

import org.ntqqrev.saltify.core.forward
import org.ntqqrev.saltify.core.node
import org.ntqqrev.saltify.core.text
import org.ntqqrev.saltify.dsl.SaltifyPlugin
import org.ntqqrev.saltify.entity.RegisteredCommand
import org.ntqqrev.saltify.entity.RegisteredSubCommand
import org.ntqqrev.saltify.extension.command
import org.ntqqrev.saltify.extension.respond

/**
 * Saltify 内置全局帮助指令插件。
 */
public val commandHelp: SaltifyPlugin<Unit> = SaltifyPlugin("command-help") {
    command("help") {
        description = "显示所有已注册指令的帮助信息"

        onExecute {
            val registry = client.commandRegistry
            if (registry.isEmpty()) {
                respond("当前没有已注册的指令。")
                return@onExecute
            }

            val grouped = registry
                .groupBy { it.pluginName }
                .entries
                .sortedWith(compareBy { it.key ?: "\uFFFF" })

            respond {
                forward {
                    node(event.senderId, "Command Help") {
                        text(buildString {
                            appendLine("指令帮助")
                            append("当前共注册了 ${registry.size} 条指令")
                        })
                    }

                    for ((pluginName, commands) in grouped) {
                        node(event.senderId, "Command Help") {
                            text(buildCommandGroupText(pluginName, commands))
                        }
                    }
                }
            }
        }
    }
}

private fun buildCommandGroupText(
    pluginName: String?,
    commands: List<RegisteredCommand>
): String = buildString {
    if (pluginName != null) {
        appendLine("插件 $pluginName: ")
    } else {
        appendLine("其他指令: ")
    }

    for (cmd in commands) {
        appendLine()
        // 指令名 + 描述
        val header = "${cmd.prefix}${cmd.name}"
        if (cmd.description.isNotEmpty()) {
            appendLine("$header - ${cmd.description}")
        } else {
            appendLine(header)
        }
        // 参数
        if (cmd.parameters.isNotEmpty()) {
            val params = cmd.parameters.joinToString(" ") { p ->
                "<${p.name}: ${p.type.simpleName ?: p.type}>"
            }
            appendLine("  参数: $params")
        }
        // 子指令
        for (sub in cmd.subCommands) {
            appendSubCommand(sub, "${cmd.prefix}${cmd.name}", depth = 1)
        }
    }
}.trimEnd()

private fun StringBuilder.appendSubCommand(
    sub: RegisteredSubCommand,
    parentPath: String,
    depth: Int
) {
    val indent = "  ".repeat(depth)
    val path = "$parentPath ${sub.name}"
    if (sub.description.isNotEmpty()) {
        appendLine("$indent$path - ${sub.description}")
    } else {
        appendLine("$indent$path")
    }
    if (sub.parameters.isNotEmpty()) {
        val params = sub.parameters.joinToString(" ") { p ->
            "<${p.name}: ${p.type.simpleName ?: p.type}>"
        }
        appendLine("$indent  参数: $params")
    }
    for (nested in sub.subCommands) {
        appendSubCommand(nested, path, depth + 1)
    }
}
