package org.ntqqrev.saltify.dsl

import org.ntqqrev.saltify.annotation.SaltifyDsl
import org.ntqqrev.saltify.model.EventConnectionType

@Suppress("MagicNumber")
@SaltifyDsl
public class EventConnectionConfig {
    /**
     * 事件服务使用的协议。
     */
    public var type: EventConnectionType = EventConnectionType.WebSocket

    /**
     * 是否启用自动重连。
     */
    public var autoReconnect: Boolean = true

    /**
     * 最小自动重连间隔。
     */
    public var baseReconnectionInterval: Long = 500L

    /**
     * 最大自动重连间隔。
     */
    public var maxReconnectionInterval: Long = 10000L

    /**
     * 最大重试次数。
     */
    public var maxReconnectionAttempts: Int = 5
}
