package org.ntqqrev.saltify.dsl.config

import org.ntqqrev.saltify.annotation.SaltifyDsl
import org.ntqqrev.saltify.dsl.SaltifyPlugin
import org.ntqqrev.saltify.dsl.SaltifyPluginContext
import org.ntqqrev.saltify.entity.InstalledPlugin

@SaltifyDsl
public class SaltifyApplicationConfig {
    /**
     * 接口根地址，如：`https://localhost:3000`。
     */
    public var addressBase: String = ""

    internal var eventConnectionConfig = EventConnectionConfig()

    /**
     * 事件服务配置。
     */
    public fun eventConnection(block: EventConnectionConfig.() -> Unit) {
        eventConnectionConfig.block()
    }

    /**
     * 访问令牌，无需 Bearer。
     */
    public var accessToken: String? = null

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
