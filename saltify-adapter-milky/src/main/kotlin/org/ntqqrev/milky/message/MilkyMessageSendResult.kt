package org.ntqqrev.milky.message

import kotlinx.datetime.Instant
import org.ntqqrev.milky.MilkyContext
import org.ntqqrev.saltify.message.outgoing.MessageSendResult

class MilkyMessageSendResult(
    override val ctx: MilkyContext,
    override val sequence: Long,
    override val time: Instant,
) : MessageSendResult