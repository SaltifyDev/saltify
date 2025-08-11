package org.ntqqrev.milky.message

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import org.ntqqrev.milky.MilkyContext
import org.ntqqrev.milky.protocol.message.*
import org.ntqqrev.saltify.getMessages
import org.ntqqrev.saltify.getTempUrl
import org.ntqqrev.saltify.message.incoming.*
import org.ntqqrev.saltify.message.outgoing.ForwardFeature
import org.ntqqrev.saltify.message.outgoing.ForwardMessageBuilder

class MilkyForwardPacker(val ctx: MilkyContext) : ForwardFeature.Packer {
    private val deferredData = mutableListOf<Deferred<MilkyOutgoingForwardedMessageData>>()

    private fun MutableList<Deferred<MilkyOutgoingForwardedMessageData>>.addAsync(
        block: suspend () -> MilkyOutgoingForwardedMessageData
    ) {
        this.add(ctx.env.scope.async { block() })
    }

    override fun fake(
        uin: Long,
        name: String,
        builder: ForwardMessageBuilder.() -> Unit
    ) {
        val forwardBuilder = MilkyUniversalMessageBuilder(ctx)
        forwardBuilder.builder()
        deferredData.addAsync {
            MilkyOutgoingForwardedMessageData(
                userId = uin,
                senderName = name,
                segments = forwardBuilder.build()
            )
        }
    }

    override fun existing(incomingMessage: IncomingMessage) {
        deferredData.addAsync {
            MilkyOutgoingForwardedMessageData(
                incomingMessage.senderUin, incomingMessage.senderName,
                segments = incomingMessage.segments.map {
                    MilkyOutgoingSegmentModel(it.toMilkyOutgoingData())
                }
            )
        }
    }

    private suspend fun Segment.toMilkyOutgoingData(): MilkyOutgoingData =
        when (this) {
            is TextSegment -> MilkyOutgoingTextData(text)
            is MentionSegment -> if (uin != null)
                MilkyOutgoingMentionData(uin!!)
            else
                MilkyOutgoingMentionAllData()

            is FaceSegment -> MilkyOutgoingFaceData(id)
            is ReplySegment -> MilkyOutgoingReplyData(repliedSequence)
            is ImageSegment -> MilkyOutgoingImageData(
                uri = getTempUrl(),
                subType = subType.name,
                summary = summary
            )

            is RecordSegment -> MilkyOutgoingRecordData(getTempUrl())
            is VideoSegment -> MilkyOutgoingVideoData(getTempUrl())
            is ForwardSegment -> MilkyOutgoingForwardData(getMessages().map {
                MilkyOutgoingForwardedMessageData(
                    userId = 0,
                    senderName = it.senderName,
                    segments = it.segments.map { segment ->
                        MilkyOutgoingSegmentModel(segment.toMilkyOutgoingData())
                    }
                )
            })

            else -> throw IllegalArgumentException("Unsupported segment type: ${this::class.simpleName}")
        }

    internal suspend fun build(): List<MilkyOutgoingForwardedMessageData> =
        deferredData.map { it.await() }
}