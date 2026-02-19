package org.ntqqrev.milky.dsl

import org.ntqqrev.milky.entity.EventConnectionType

@MilkyDsl
public class MilkyClientConfig {
    public var addressBase: String = ""
    public var eventConnectionType: EventConnectionType = EventConnectionType.WebSocket
    public var accessToken: String? = null
    internal val installedPlugins = mutableListOf<MilkyPlugin>()

    public fun install(plugin: MilkyPlugin) {
        installedPlugins.add(plugin)
    }

    public fun plugin(name: String, block: MilkyPluginContext.() -> Unit) {
        install(milkyPlugin(name, block))
    }
}
