package org.ntqqrev.saltify.dsl

import io.ktor.util.logging.*
import kotlinx.coroutines.CoroutineScope
import org.ntqqrev.saltify.annotation.SaltifyDsl
import org.ntqqrev.saltify.core.SaltifyApplication
import org.ntqqrev.saltify.entity.env.ApplicationEnvironment

@SaltifyDsl
public class SaltifyPluginContext internal constructor(
    pluginName: String,
    public override val client: SaltifyApplication,
    @PublishedApi internal val pluginScope: CoroutineScope
) : CoroutineScope by pluginScope, ApplicationEnvironment(client) {
    public val logger: Logger = KtorSimpleLogger("Saltify/plugin:$pluginName")

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
}

/**
 * 一个插件
 */
public class SaltifyPlugin<T : Any>(
    public val name: String,
    @PublishedApi internal val createConfig: () -> T,
    internal val setup: SaltifyPluginContext.(T) -> Unit
) {
    /**
     * 创建一个插件。
     */
    public companion object {
        public operator fun <T : Any> invoke(
            name: String = generateAnonymousName(),
            config: () -> T,
            setup: SaltifyPluginContext.(config: T) -> Unit
        ): SaltifyPlugin<T> = SaltifyPlugin(name, config, setup)

        public operator fun invoke(
            name: String = generateAnonymousName(),
            setup: SaltifyPluginContext.(Unit) -> Unit
        ): SaltifyPlugin<Unit> = SaltifyPlugin(name, {}, setup)
    }
}

@Suppress("MagicNumber")
private fun generateAnonymousName(): String =
    "anonymous-${(1..4).map { ('A'..'Z').random() }.joinToString("")}"
