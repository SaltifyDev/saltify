package org.ntqqrev.milky

import org.ntqqrev.saltify.config.Configurable
import org.ntqqrev.saltify.config.LongRange

class MilkyInit(
    @Configurable(
        "Milky 服务 URL",
        "不包含 http:// 或 https:// 部分；仅支持 HTTP API + WebSocket 事件推送"
    )
    val milkyUrl: String = "localhost:8080/",

    @Configurable(
        "是否使用 HTTPS 协议",
    )
    val useHttps: Boolean = false,

    @Configurable(
        "Milky 的 Access Token"
    )
    val milkyAccessToken: String = "",

    @Configurable(
        "WebSocket 重连间隔",
        "单位毫秒 (ms)"
    )
    @LongRange(min = 5000L)
    val wsReconnectInterval: Long = 5000L,
)