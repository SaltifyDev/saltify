package org.ntqqrev.milky

import kotlinx.serialization.Serializable
import org.ntqqrev.saltify.config.Configurable
import org.ntqqrev.saltify.config.LongRange

@Serializable
class MilkyInit {
    @Configurable(
        "Milky 服务 URL",
        "仅支持 HTTP API + WebSocket 事件推送"
    )
    val milkyUrl: String = "http://localhost:8080/"

    @Configurable(
        "Milky 的 Access Token"
    )
    val milkyAccessToken: String = ""

    @Configurable(
        "WebSocket 重连间隔",
        "单位毫秒 (ms)"
    )
    @LongRange(min = 5000L)
    val wsReconnectInterval: Long = 5000L
}