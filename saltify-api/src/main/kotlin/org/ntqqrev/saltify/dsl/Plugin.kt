package org.ntqqrev.saltify.dsl

import org.ntqqrev.saltify.Environment
import org.ntqqrev.saltify.event.Event
import kotlin.reflect.KClass

class PluginSpec<T : Any>(
    val configClass: KClass<T>,
    val block: PluginDslContext<T>.() -> Unit
)

interface PluginDslContext<T> : Environment {
    /**
     * The configuration instance for the plugin.
     */
    val config: T

    /**
     * Executes the given block when the plugin is started by the framework.
     */
    fun onStart(block: suspend () -> Unit)

    /**
     * Registers a command with the specified name, description, and aliases.
     * The block will be executed when the command is invoked.
     * @param name The name of the command.
     * @param description A brief description of the command.
     * @param aliases A list of alternative names for the command.
     * @param block The block to execute when the command is invoked.
     */
    fun command(
        name: String,
        description: String = "",
        aliases: List<String> = emptyList(),
        block: CommandDslContext.() -> Unit
    )

    /**
     * Executes the given block when an event of the specified type is received.
     */
    fun <T : Event> on(clazz: KClass<T>, block: suspend (T) -> Unit)

    /**
     * Executes the given block when the plugin is stopped.
     */
    fun onStop(block: suspend () -> Unit)
}

inline fun <reified T : Event> PluginDslContext<*>.on(
    noinline block: suspend (T) -> Unit
) {
    on(T::class, block)
}

/**
 * Declares a plugin with the specified configuration class and block.
 */
inline fun <reified T : Any> plugin(
    noinline block: PluginDslContext<T>.() -> Unit
): PluginSpec<T> {
    return PluginSpec(T::class, block)
}