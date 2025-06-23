package org.ntqqrev.saltify

import org.ntqqrev.saltify.config.Configurable
import org.ntqqrev.saltify.config.IntRange
import org.ntqqrev.saltify.config.Option

class SaltifyAppConfig(
    @Configurable(
        "事件缓冲区长度",
        "最多缓存的事件数量，设置更小的值可节约内存，但可能导致事件丢失"
    )
    val eventBufferSize: Int = 1000,

    @Configurable(
        "网络配置"
    )
    val network: Network = Network(),

    @Configurable(
        "命令配置"
    )
    val command: Command = Command(),
) {
    class Network(
        @Configurable(
            "Saltify 服务监听地址",
            "如需公网访问，请修改为 0.0.0.0"
        )
        val host: String = "127.0.0.1",

        @Configurable(
            "Saltify 服务监听端口",
        )
        @IntRange(1, 65535)
        val port: Int = 29373, // "SLT" as radix 32
    )

    class Command(
        @Configurable(
            "命令触发策略"
        )
        val triggerPolicy: TriggerPolicy = TriggerPolicy.ON_MENTION,

        @Configurable(
            "命令触发前缀",
            "仅在命令触发策略为 ON_PREFIX 或 ON_MENTION_WITH_PREFIX 时有效"
        )
        val triggerPrefix: String = "/",
    ) {
        enum class TriggerPolicy {
            @Option("在提及机器人时触发命令，例如 @Bot help")
            ON_MENTION,

            @Option("在消息以特定字符起始时触发命令，例如 /help")
            ON_PREFIX,

            @Option("在提及机器人并且以特定字符起始时触发命令，例如 @Bot /help")
            ON_MENTION_WITH_PREFIX,
        }
    }
}