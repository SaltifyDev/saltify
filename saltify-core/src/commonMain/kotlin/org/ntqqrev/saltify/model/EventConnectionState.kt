package org.ntqqrev.saltify.model

import org.ntqqrev.saltify.core.SaltifyApplication

/**
 * Saltify 应用的事件服务连接状态
 */
public sealed class EventConnectionState {
    public data class Connected(
        val type: EventConnectionType,
        val instance: SaltifyApplication
    ) : EventConnectionState()
    public data class Disconnected(val throwable: Throwable?) : EventConnectionState()
    public data class Reconnecting(val throwable: Throwable, val attempt: Int) : EventConnectionState()
    public object Connecting : EventConnectionState()
}
