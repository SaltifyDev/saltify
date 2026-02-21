package org.ntqqrev.saltify.dsl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.ntqqrev.milky.Event
import org.ntqqrev.milky.IncomingMessage
import org.ntqqrev.milky.OutgoingSegment
import org.ntqqrev.saltify.annotation.ContextParametersMigrationNeeded
import org.ntqqrev.saltify.annotation.SaltifyDsl
import org.ntqqrev.saltify.core.SaltifyApplication
import org.ntqqrev.saltify.core.recallGroupMessage
import org.ntqqrev.saltify.core.recallPrivateMessage
import org.ntqqrev.saltify.extension.command
import org.ntqqrev.saltify.extension.on
import org.ntqqrev.saltify.extension.respond
import org.ntqqrev.saltify.model.milky.SendMessageOutput
import kotlin.time.Duration

@SaltifyDsl
public class SaltifyPluginContext internal constructor(
    public val client: SaltifyApplication,
    @PublishedApi internal val pluginScope: CoroutineScope
) : CoroutineScope by pluginScope {
    internal val onStartHooks = mutableListOf<suspend () -> Unit>()
    internal val onStopHooks = mutableListOf<() -> Unit>()

    /**
     * 插件被加载，即 [SaltifyApplication.Companion.invoke] 后执行的逻辑。
     */
    public fun onStart(block: suspend () -> Unit) {
        onStartHooks.add(block)
    }

    /**
     * 插件被卸载，即 [SaltifyApplication.close] 前执行的逻辑。
     */
    public fun onStop(block: () -> Unit) {
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
    public suspend fun Event.MessageReceive.respond(
        block: MutableList<OutgoingSegment>.() -> Unit
    ): SendMessageOutput = respond(client, block)

    /**
     * 响应事件，并在指定延迟后撤回消息。
     */
    @ContextParametersMigrationNeeded
    public suspend inline fun Event.MessageReceive.respondWithRecall(
        delay: Duration,
        noinline block: MutableList<OutgoingSegment>.() -> Unit
    ) {
        val output = respond(block)
        delay(delay)
        when (data) {
            is IncomingMessage.Group -> client.recallGroupMessage(data.peerId, output.messageSeq)
            else -> client.recallPrivateMessage(data.peerId, output.messageSeq)
        }
    }
}

/**
 * 一个插件
 */
public class SaltifyPlugin(
    public val name: String,
    internal val setup: SaltifyPluginContext.() -> Unit
)

/**
 * 创建一个插件。
 */
public fun createSaltifyPlugin(
    name: String = "unspecified",
    block: SaltifyPluginContext.() -> Unit
): SaltifyPlugin = SaltifyPlugin(name, block)
