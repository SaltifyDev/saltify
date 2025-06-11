package org.ntqqrev.lagrange

import org.ntqqrev.saltify.config.Configurable

class LagrangeInit {
    @Configurable(
        "签名 API URL"
    )
    val signApiUrl: String = "https://sign.lagrangecore.org/api/sign/30366"

    @Configurable(
        "签名 API 代理 URL",
        "如果访问签名 API URL 失败，可以尝试使用代理，仅支持 HTTP 代理"
    )
    val signApiProxyUrl: String = ""

    @Configurable(
        "QQ 号",
        "必须与扫码时所用 QQ 号相同，否则无法正常自动登录"
    )
    val uin: Long = 0L
}