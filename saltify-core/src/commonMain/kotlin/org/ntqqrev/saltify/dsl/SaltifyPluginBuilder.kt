package org.ntqqrev.saltify.dsl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.ntqqrev.milky.Event
import org.ntqqrev.milky.OutgoingSegment
import org.ntqqrev.saltify.annotation.SaltifyDsl
import org.ntqqrev.saltify.core.SaltifyApplication
import org.ntqqrev.saltify.extension.command
import org.ntqqrev.saltify.extension.on
import org.ntqqrev.saltify.extension.respond

@SaltifyDsl
public class SaltifyPluginBuilder internal constructor(
    public val client: SaltifyApplication,
    @PublishedApi internal val pluginScope: CoroutineScope
) : CoroutineScope by pluginScope {
    internal val onStartHooks = mutableListOf<suspend () -> Unit>()
    internal val onStopHooks = mutableListOf<suspend () -> Unit>()

    public fun onStart(block: suspend () -> Unit) {
        onStartHooks.add(block)
    }

    public fun onStop(block: suspend () -> Unit) {
        onStopHooks.add(block)
    }

    public inline fun <reified T : Event> on(
        crossinline block: suspend SaltifyApplication.(event: T) -> Unit
    ): Job = client.on(pluginScope, block)

    public fun command(
        name: String,
        prefix: String = "/",
        block: SaltifyCommandContext.() -> Unit
    ): Job = client.command(name, prefix, pluginScope, block)

    public suspend fun Event.MessageReceive.respond(block: MutableList<OutgoingSegment>.() -> Unit): Any =
        respond(client, block)
}

public class SaltifyPlugin(
    public val name: String,
    internal val setup: SaltifyPluginBuilder.() -> Unit
)

public fun createSaltifyPlugin(
    name: String = "unspecified",
    block: SaltifyPluginBuilder.() -> Unit
): SaltifyPlugin = SaltifyPlugin(name, block)
