package org.ntqqrev.saltify.dsl

import io.ktor.util.logging.*
import kotlinx.coroutines.CoroutineScope
import org.ntqqrev.saltify.annotation.SaltifyDsl
import org.ntqqrev.saltify.SaltifyApplication
import org.ntqqrev.saltify.runtime.context.ApplicationContext

@SaltifyDsl
public class PluginBuilder internal constructor(
    public override val client: SaltifyApplication,
    @PublishedApi internal val pluginScope: CoroutineScope,
    pluginName: String
) : CoroutineScope by pluginScope, ApplicationContext(client) {
    internal val onStartHooks = mutableListOf<suspend () -> Unit>()
    internal val onStopHooks = mutableListOf<() -> Unit>()

    public val logger: Logger = KtorSimpleLogger("Saltify/plugin:$pluginName")

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
}

/**
 * 一个插件
 */
public class SaltifyPlugin<T : Any>(
    public val name: String,
    @PublishedApi internal val createConfig: () -> T,
    internal val setup: PluginBuilder.(T) -> Unit
) {
    public companion object {
        /**
         * 创建一个插件。
         *
         * @param name 插件名，不填随机。
         */
        public operator fun <T : Any> invoke(
            name: String = generateAnonymousName(),
            config: () -> T,
            setup: PluginBuilder.(config: T) -> Unit
        ): SaltifyPlugin<T> = SaltifyPlugin(name, config, setup)

        /**
         * 创建一个插件。
         *
         * @param name 插件名，不填随机。
         */
        public operator fun invoke(
            name: String = generateAnonymousName(),
            setup: PluginBuilder.(Unit) -> Unit
        ): SaltifyPlugin<Unit> = SaltifyPlugin(name, {}, setup)
    }
}

private fun generateAnonymousName(): String =
    "anonymous-${(1..4).map { ('A'..'Z').random() }.joinToString("")}"
