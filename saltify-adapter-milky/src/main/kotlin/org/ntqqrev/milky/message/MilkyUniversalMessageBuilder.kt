package org.ntqqrev.milky.message

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import org.ntqqrev.milky.MilkyContext
import org.ntqqrev.milky.protocol.message.*
import org.ntqqrev.milky.util.toMilkyUri
import org.ntqqrev.saltify.message.ImageSubType
import org.ntqqrev.saltify.message.outgoing.*

class MilkyUniversalMessageBuilder(override val ctx: MilkyContext) :
    PrivateMessageBuilder,
    GroupMessageBuilder,
    ForwardMessageBuilder {

    private val deferredData = mutableListOf<Deferred<MilkyOutgoingData>>()

    private fun MutableList<Deferred<MilkyOutgoingData>>.addAsync(
        block: suspend () -> MilkyOutgoingData
    ) {
        this.add(ctx.env.scope.async { block() })
    }

    override fun text(text: String) {
        deferredData.addAsync { MilkyOutgoingTextData(text) }
    }

    override fun face(id: String) {
        deferredData.addAsync { MilkyOutgoingFaceData(id) }
    }

    override fun image(
        resource: ResourceLocation,
        subType: ImageSubType,
        summary: String?
    ) {
        deferredData.addAsync {
            MilkyOutgoingImageData(
                uri = resource.toMilkyUri(),
                subType = subType.name,
                summary = summary
            )
        }
    }

    override fun record(resource: ResourceLocation) {
        deferredData.addAsync { MilkyOutgoingRecordData(resource.toMilkyUri()) }
    }

    override fun reply(sequence: Long) {
        deferredData.addAsync { MilkyOutgoingReplyData(sequence) }
    }

    override fun forward(packer: ForwardFeature.Packer.() -> Unit) {
        deferredData.addAsync {
            val forwardPacker = MilkyForwardPacker(ctx)
            forwardPacker.packer()
            MilkyOutgoingForwardData(forwardPacker.build())
        }
    }

    override fun mention(uin: Long) {
        deferredData.addAsync { MilkyOutgoingMentionData(uin) }
    }

    override fun mentionAll() {
        deferredData.addAsync { MilkyOutgoingMentionAllData() }
    }

    override fun video(
        resource: ResourceLocation,
        cover: ResourceLocation?
    ) {
        deferredData.addAsync {
            MilkyOutgoingVideoData(
                uri = resource.toMilkyUri(),
                thumbUri = cover?.toMilkyUri()
            )
        }
    }

    internal suspend fun build(): List<MilkyOutgoingSegmentModel> {
        return deferredData.map { it.await() }.map { MilkyOutgoingSegmentModel(it) }
    }
}