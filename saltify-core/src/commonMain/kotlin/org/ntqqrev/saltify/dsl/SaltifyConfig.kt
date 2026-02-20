package org.ntqqrev.saltify.dsl

import org.ntqqrev.saltify.annotation.SaltifyDsl
import org.ntqqrev.saltify.entity.EventConnectionType

@SaltifyDsl
public class SaltifyConfig {
    public var addressBase: String = ""
    public var eventConnectionType: EventConnectionType = EventConnectionType.WebSocket
    public var accessToken: String? = null
    internal val installedPlugins = mutableListOf<SaltifyPlugin>()

    public fun install(plugin: SaltifyPlugin) {
        installedPlugins.add(plugin)
    }

    public fun plugin(name: String, block: SaltifyPluginBuilder.() -> Unit) {
        install(createSaltifyPlugin(name, block))
    }
}
