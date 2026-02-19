package org.ntqqrev.milky.dsl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.ntqqrev.milky.Event
import org.ntqqrev.milky.IncomingMessage
import org.ntqqrev.milky.OutgoingSegment
import org.ntqqrev.milky.annotation.MilkyDsl
import org.ntqqrev.milky.core.MilkyClient
import org.ntqqrev.milky.core.sendGroupMessage
import org.ntqqrev.milky.core.sendPrivateMessage
import org.ntqqrev.milky.extension.command
import org.ntqqrev.milky.extension.on

@MilkyDsl
public class MilkyPluginDsl internal constructor(
    public val client: MilkyClient,
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
        crossinline block: suspend MilkyClient.(event: T) -> Unit
    ): Job = launch { client.on(block) }

    public fun command(
        name: String,
        prefix: String = "/",
        block: MilkyCommandDsl.() -> Unit
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

public class MilkyPlugin(
    public val name: String,
    internal val setup: MilkyPluginDsl.() -> Unit
)

public fun milkyPlugin(name: String, block: MilkyPluginDsl.() -> Unit): MilkyPlugin {
    return MilkyPlugin(name, block)
}
