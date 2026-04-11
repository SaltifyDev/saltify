package org.ntqqrev.saltify.model.event

import org.ntqqrev.saltify.SaltifyApplication

/**
 * Saltify 应用的事件服务连接状态
 */
public sealed class EventConnectionState {
    public data class Connected(
        val type: EventConnectionType,
        val instance: SaltifyApplication
    ) : EventConnectionState()
    public data class Disconnected(val throwable: Throwable?) : EventConnectionState()
    public data class Reconnecting(val throwable: Throwable, val attempt: Int, val delay: Long) : EventConnectionState()
    public object Connecting : EventConnectionState()
}
