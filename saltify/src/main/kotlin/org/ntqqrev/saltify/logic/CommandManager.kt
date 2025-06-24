package org.ntqqrev.saltify.logic

import io.github.oshai.kotlinlogging.KotlinLogging
import org.ntqqrev.saltify.SaltifyApp
import org.ntqqrev.saltify.SaltifyAppConfig
import org.ntqqrev.saltify.command.Command
import org.ntqqrev.saltify.command.MessageTokenizer
import org.ntqqrev.saltify.command.TextToken
import org.ntqqrev.saltify.exception.CommandNotFoundException
import org.ntqqrev.saltify.message.incoming.IncomingMessage
import org.ntqqrev.saltify.message.incoming.MentionSegment
import org.ntqqrev.saltify.message.incoming.TextSegment
import org.ntqqrev.saltify.plugin.Plugin
import java.util.concurrent.ConcurrentHashMap

class CommandManager(
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

    suspend fun process(message: IncomingMessage) {
        var tokenizer: MessageTokenizer? = null
        var commandLiteral: String? = null
        when (app.config.command.triggerPolicy) {
            SaltifyAppConfig.Command.TriggerPolicy.ON_MENTION -> {
                val mentionIndex = message.segments.indexOfFirst { it is MentionSegment }
                if (mentionIndex == -1) {
                    return
                }
                val mentionSegment = message.segments[mentionIndex] as MentionSegment
                if (
                    message.ctx.getLoginInfo().first == mentionSegment.uin
                    && message.segments.size > mentionIndex + 1
                    && message.segments[mentionIndex + 1] is TextSegment
                ) {
                    tokenizer = MessageTokenizer(
                        message,
                        mentionIndex + 1
                    )
                    commandLiteral = (tokenizer.read() as TextToken).text
                }
            }

            SaltifyAppConfig.Command.TriggerPolicy.ON_PREFIX -> {
                if (message.segments.first() is TextSegment) {
                    tokenizer = MessageTokenizer(message)
                    val firstToken = tokenizer.read() as TextToken
                    if (firstToken.text.startsWith(app.config.command.triggerPrefix)) {
                        commandLiteral = firstToken.text.removePrefix(app.config.command.triggerPrefix)
                    }
                }
            }

            SaltifyAppConfig.Command.TriggerPolicy.ON_MENTION_WITH_PREFIX -> {
                val mentionIndex = message.segments.indexOfFirst { it is MentionSegment }
                if (mentionIndex == -1) {
                    return
                }
                val mentionSegment = message.segments[mentionIndex] as MentionSegment
                if (
                    message.ctx.getLoginInfo().first == mentionSegment.uin
                    && message.segments.size > mentionIndex + 1
                    && message.segments[mentionIndex + 1] is TextSegment
                ) {
                    tokenizer = MessageTokenizer(
                        message,
                        mentionIndex + 1
                    )
                    val firstToken = tokenizer.read() as TextToken
                    if (firstToken.text.startsWith(app.config.command.triggerPrefix)) {
                        commandLiteral = firstToken.text.removePrefix(app.config.command.triggerPrefix)
                    }
                }
            }
        }

        if (tokenizer == null || commandLiteral == null) {
            return
        }
        val commandName = aliases[commandLiteral] ?: commandLiteral
        val command = commands[commandName]
        if (command == null) {
            throw CommandNotFoundException(commandName)
        }
        command.tryExecute(tokenizer, message).also {
            logger.info { "Command '$commandName' executed by ${message.sender.uin}" }
        }
    }
}