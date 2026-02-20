package org.ntqqrev.saltify.dsl

import org.ntqqrev.saltify.annotation.SaltifyDsl
import org.ntqqrev.saltify.model.EventConnectionType

@SaltifyDsl
public class SaltifyConfig {
    /**
     * 接口根地址，如：`https://localhost:3000`。
     */
    public var addressBase: String = ""

    /**
     * 事件服务使用的协议。
     */
    public var eventConnectionType: EventConnectionType = EventConnectionType.WebSocket

    /**
     * 访问令牌，无需 Bearer。
     */
    public var accessToken: String? = null
    internal val installedPlugins = mutableListOf<SaltifyPlugin>()

    public fun install(plugin: SaltifyPlugin) {
        installedPlugins.add(plugin)
    }

    /**
     * 定义并安装插件，用法与 [createSaltifyPlugin] 相同。
     */
    public fun plugin(name: String = "unspecified", block: SaltifyPluginContext.() -> Unit) {
        install(createSaltifyPlugin(name, block))
    }
}
