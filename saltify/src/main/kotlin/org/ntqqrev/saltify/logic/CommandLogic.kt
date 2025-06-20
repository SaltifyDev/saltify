package org.ntqqrev.saltify.logic

import io.github.oshai.kotlinlogging.KotlinLogging
import org.ntqqrev.saltify.SaltifyApp
import org.ntqqrev.saltify.command.Command
import org.ntqqrev.saltify.message.incoming.IncomingMessage
import org.ntqqrev.saltify.plugin.Plugin
import java.util.concurrent.ConcurrentHashMap

class CommandLogic(
    val app: SaltifyApp,
) {
    private val logger = KotlinLogging.logger {}

    val commands = ConcurrentHashMap<String, Command>()
    val aliases = ConcurrentHashMap<String, String>()

    fun register(name: String, command: Command) {
        if (commands.containsKey(name)) {
            logger.warn {
                "Command '$name' from plugin '${command.plugin.meta.id}' conflicts with " +
                        "another command from plugin '${commands[name]!!.plugin.meta.id}', " +
                        "skipping registration."
            }
            return
        }
        commands[command.name] = command
    }

    fun registerAlias(plugin: Plugin<*>, alias: String, commandName: String) {
        if (aliases.containsKey(alias)) {
            logger.warn {
                "Alias '$alias' for command '$commandName' from plugin '${plugin.meta.id}' " +
                        "conflicts with another alias for command '${aliases[alias]}', skipping registration."
            }
            return
        }
        if (!commands.containsKey(commandName)) {
            logger.warn {
                "Command '$commandName' for alias '$alias' from plugin '${plugin.meta.id}' " +
                        "does not exist, skipping alias registration."
            }
            return
        }
        if (commands[commandName]!!.plugin != plugin) {
            logger.warn {
                "Command '$commandName' for alias '$alias' from plugin '${plugin.meta.id}' " +
                        "does not belong to the same plugin, skipping alias registration."
            }
            return
        }
        aliases[alias] = commandName
    }

    fun registerAll(plugin: Plugin<*>) {
        plugin.commands.forEach { (name, command) -> register(name, command) }
        plugin.cmdAliases.forEach { (alias, commandName) ->
            registerAlias(plugin, alias, commandName)
        }
    }

    fun unregisterAll(plugin: Plugin<*>) {
        plugin.cmdAliases.forEach { (alias, _) ->
            if (aliases[alias]?.let { commands[it]?.plugin == plugin } == true) {
                aliases.remove(alias)
            }
        }
        plugin.commands.forEach { (name, _) ->
            if (commands[name]?.plugin == plugin) {
                commands.remove(name)
            }
        }
    }

    fun process(message: IncomingMessage) {
        // TODO
    }
}