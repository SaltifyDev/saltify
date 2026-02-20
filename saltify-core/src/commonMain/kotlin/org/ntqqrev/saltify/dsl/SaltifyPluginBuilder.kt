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

    /**
     * 插件被加载，即 [SaltifyApplication.connectEvent] 后执行的逻辑。
     */
    public fun onStart(block: suspend () -> Unit) {
        onStartHooks.add(block)
    }

    /**
     * 插件被卸载，即 [SaltifyApplication.disconnectEvent] 后执行的逻辑。
     */
    public fun onStop(block: suspend () -> Unit) {
        onStopHooks.add(block)
    }

    /**
     * 注册一个事件监听器。
     */
    public inline fun <reified T : Event> on(
        crossinline block: suspend SaltifyApplication.(event: T) -> Unit
    ): Job = client.on(pluginScope, block)

    /**
     * 注册一个命令。
     *
     * ```kotlin
     * command("order") {
     *     val id = parameter<Int>("id")
     *     val note = greedyStringParameter("note")
     *
     *     onExecute {
     *         respond {
     *             text("Order #${id.value} created\nnote：${note.value}")
     *         }
     *     }
     *
     *     onGroupExecute {
     *         // ...
     *     }
     *
     *     onFailure {
     *         respond {
     *             text("Command run failed: $it")
     *         }
     *     }
     * }
     * ```
     */
    public fun command(
        name: String,
        prefix: String = "/",
        block: SaltifyCommandContext.() -> Unit
    ): Job = client.command(name, prefix, pluginScope, block)

    /**
     * 响应事件。
     */
    public suspend fun Event.MessageReceive.respond(block: MutableList<OutgoingSegment>.() -> Unit): Any =
        respond(client, block)
}

/**
 * 一个插件
 */
public class SaltifyPlugin(
    public val name: String,
    internal val setup: SaltifyPluginBuilder.() -> Unit
)

/**
 * 创建一个插件。
 */
public fun createSaltifyPlugin(
    name: String = "unspecified",
    block: SaltifyPluginBuilder.() -> Unit
): SaltifyPlugin = SaltifyPlugin(name, block)
