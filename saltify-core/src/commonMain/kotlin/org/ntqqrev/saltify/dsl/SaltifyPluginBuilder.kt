package org.ntqqrev.saltify.dsl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.ntqqrev.milky.Event
import org.ntqqrev.milky.IncomingMessage
import org.ntqqrev.milky.OutgoingSegment
import org.ntqqrev.saltify.annotation.SaltifyDsl
import org.ntqqrev.saltify.core.SaltifyApplication
import org.ntqqrev.saltify.core.sendGroupMessage
import org.ntqqrev.saltify.core.sendPrivateMessage
import org.ntqqrev.saltify.extension.command
import org.ntqqrev.saltify.extension.on

@SaltifyDsl
public class SaltifyPluginBuilder internal constructor(
    public val client: SaltifyApplication,
    private val pluginScope: CoroutineScope
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
    ): Job = launch { client.on(block) }

    public fun command(
        name: String,
        prefix: String = "/",
        block: SaltifyCommandContext.() -> Unit
    ): Job = launch { client.command(name, prefix, block) }

    public suspend fun Event.MessageReceive.reply(message: List<OutgoingSegment>): Any =
        when (val data = this.data) {
            is IncomingMessage.Group -> client.sendGroupMessage(data.peerId, message)
            else -> client.sendPrivateMessage(data.peerId, message)
        }

    public suspend fun Event.MessageReceive.reply(block: MutableList<OutgoingSegment>.() -> Unit): Any =
        when (val data = this.data) {
            is IncomingMessage.Group -> client.sendGroupMessage(data.peerId, block)
            else -> client.sendPrivateMessage(data.peerId, block)
        }
}

public class SaltifyPlugin(
    public val name: String,
    internal val setup: SaltifyPluginBuilder.() -> Unit
)

public fun createSaltifyPlugin(name: String, block: SaltifyPluginBuilder.() -> Unit): SaltifyPlugin {
    return SaltifyPlugin(name, block)
}
