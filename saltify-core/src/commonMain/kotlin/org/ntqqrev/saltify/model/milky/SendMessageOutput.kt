package org.ntqqrev.saltify.model.milky

import kotlinx.serialization.Serializable

@Serializable
public data class SendMessageOutput(
    /**
     * 消息序列号
     */
    public val messageSeq: Long,
    /**
     * 消息发送时间
     */
    public val time: Long
)
