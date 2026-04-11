package org.ntqqrev.saltify.dsl.config

import org.ntqqrev.saltify.annotation.SaltifyDsl
import org.ntqqrev.saltify.dsl.PluginBuilder
import org.ntqqrev.saltify.dsl.SaltifyPlugin
import org.ntqqrev.saltify.internal.util.InstalledPlugin

@SaltifyDsl
public class ApplicationConfig {
    @PublishedApi
    internal val connection: ConnectionConfig = ConnectionConfig()

    @PublishedApi
    internal val bot: BotConfig = BotConfig()

    /**
     * Bot 实例相关设置。
     */
    public fun bot(block: BotConfig.() -> Unit) {
        bot.block()
    }

    /**
     * 连接相关设置。
     */
    public fun connection(block: ConnectionConfig.() -> Unit) {
        connection.block()
    }

    internal val installedPlugins = mutableListOf<InstalledPlugin<*>>()

    public fun <T : Any> install(plugin: SaltifyPlugin<T>, configure: T.() -> Unit = {}) {
        installedPlugins.add(InstalledPlugin(plugin, configure))
    }

    /**
     * 定义并安装插件，用法与 [SaltifyPlugin] 相同。
     */
    public fun plugin(name: String = "unspecified", block: PluginBuilder.(Unit) -> Unit) {
        install(SaltifyPlugin(name, {}, block))
    }
}
