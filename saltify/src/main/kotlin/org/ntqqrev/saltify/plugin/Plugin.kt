package org.ntqqrev.saltify.plugin

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.ntqqrev.saltify.Environment
import org.ntqqrev.saltify.command.Command
import org.ntqqrev.saltify.dsl.CommandDslContext
import org.ntqqrev.saltify.dsl.PluginDslContext
import org.ntqqrev.saltify.event.Event
import kotlin.reflect.KClass

class Plugin<T>(
    internal val meta: PluginMeta,
    override val config: T,
    internal val env: Environment,
    internal val flow: SharedFlow<Event>,
) : PluginDslContext<T>, Environment by env {
    private val logger = KotlinLogging.logger {}

    var onStartBlock: suspend () -> Unit = {}
    var onStopBlock: suspend () -> Unit = {}
    val eventHandler: MutableMap<KClass<out Event>, suspend (Event) -> Unit> = mutableMapOf()

    val registeredCommand = mutableMapOf<String, Command>()
    val registeredCommandAliases = mutableMapOf<String, String>()

    override fun onStart(block: suspend () -> Unit) {
        onStartBlock = block
    }

    override fun command(
        name: String,
        description: String,
        aliases: List<String>,
        block: CommandDslContext.() -> Unit
    ) {
        if (registeredCommand.containsKey(name)) {
            logger.warn { "Command '$name' in plugin '${meta.id}' is already registered, overwriting." }
        }
        val command = Command(name, description)
        block(command)
        registeredCommand[name] = command
        for (alias in aliases) {
            if (registeredCommandAliases.containsKey(alias)) {
                logger.warn { "Alias '$alias' in plugin '${meta.id}' is already registered, overwriting." }
            }
            registeredCommandAliases[alias] = name
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Event> subscribe(
        clazz: KClass<T>,
        block: suspend T.() -> Unit
    ) {
        if (eventHandler.containsKey(clazz)) {
            logger.warn { "Event handler in plugin '${meta.id}' for ${clazz.simpleName} is already registered, overwriting." }
        }
        eventHandler[clazz] = block as suspend (Event) -> Unit
    }

    override fun onStop(block: suspend () -> Unit) {
        onStopBlock = block
    }

    suspend fun start() {
        onStartBlock()
        scope.launch {
            flow.collect {
                val handler = eventHandler[it::class]
                if (handler != null) {
                    try {
                        handler(it)
                    } catch (e: Exception) {
                        logger.error(e) { "Error handling event ${it::class.simpleName} in plugin '${meta.id}': ${e.message}" }
                    }
                }
            }
        }
    }

    suspend fun stop() {
        onStopBlock()
    }
}