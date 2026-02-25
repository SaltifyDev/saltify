package org.ntqqrev.saltify.dsl.config

import org.ntqqrev.saltify.annotation.SaltifyDsl

@SaltifyDsl
public class ConnectionConfig {
    /**
     * 基础 URL 地址。
     */
    public var baseUrl: String = ""

    /**
     * 访问令牌，无需 Bearer。
     */
    public var accessToken: String? = null

    internal var event = EventConnectionConfig()

    /**
     * 事件服务配置。
     */
    public fun events(block: EventConnectionConfig.() -> Unit) {
        event.block()
    }
}
