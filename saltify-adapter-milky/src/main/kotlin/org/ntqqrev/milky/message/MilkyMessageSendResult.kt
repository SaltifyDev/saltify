package org.ntqqrev.milky.message

import kotlinx.datetime.Instant
import org.ntqqrev.saltify.Context
import org.ntqqrev.saltify.message.outgoing.MessageSendResult

class MilkyMessageSendResult(
    override val ctx: Context,
    override val sequence: Long,
    override val time: Instant,
) : MessageSendResult