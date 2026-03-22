package org.ntqqrev.saltify.dsl.config

import org.ntqqrev.saltify.annotation.SaltifyDsl
import org.ntqqrev.saltify.dsl.SaltifyPlugin
import org.ntqqrev.saltify.dsl.SaltifyPluginContext
import org.ntqqrev.saltify.entity.SaltifyBotConfig
import org.ntqqrev.saltify.entity.InstalledPlugin

@SaltifyDsl
public class SaltifyApplicationConfig {
    internal val connection = ConnectionConfig()

    /**
     * Bot 实例相关设置。
     */
    public fun bot(block: SaltifyBotConfig.() -> Unit) {
        SaltifyBotConfig.block()
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
    public fun plugin(name: String = "unspecified", block: SaltifyPluginContext.(Unit) -> Unit) {
        install(SaltifyPlugin(name, {}, block))
    }
}
