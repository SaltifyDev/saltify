package org.ntqqrev.milky.model.message

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

internal sealed class MilkyOutgoingSegmentModel(
    val type: String,
    val data: MilkyOutgoingData,
)

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(MilkyOutgoingTextData::class, "text"),
    JsonSubTypes.Type(MilkyOutgoingMentionData::class, "mention"),
    JsonSubTypes.Type(MilkyOutgoingMentionAllData::class, "mention_all"),
    JsonSubTypes.Type(MilkyOutgoingFaceData::class, "face"),
    JsonSubTypes.Type(MilkyOutgoingReplyData::class, "reply"),
    JsonSubTypes.Type(MilkyOutgoingImageData::class, "image"),
    JsonSubTypes.Type(MilkyOutgoingRecordData::class, "record"),
    JsonSubTypes.Type(MilkyOutgoingVideoData::class, "video"),
    JsonSubTypes.Type(MilkyOutgoingForwardData::class, "forward"),
)
internal sealed class MilkyOutgoingData

internal class MilkyOutgoingTextData(
    val text: String,
) : MilkyOutgoingData()

internal class MilkyOutgoingMentionData(
    val userId: Long,
) : MilkyOutgoingData()

internal class MilkyOutgoingMentionAllData(
) : MilkyOutgoingData()

internal class MilkyOutgoingFaceData(
    val faceId: String,
) : MilkyOutgoingData()

internal class MilkyOutgoingReplyData(
    val messageSeq: Long,
) : MilkyOutgoingData()

internal class MilkyOutgoingImageData(
    val uri: String,
    val summary: String? = null,
    val subType: String? = null,
) : MilkyOutgoingData()

internal class MilkyOutgoingRecordData(
    val uri: String,
) : MilkyOutgoingData()

internal class MilkyOutgoingVideoData(
    val uri: String,
    val thumbUri: String? = null,
) : MilkyOutgoingData()

internal class MilkyOutgoingForwardData(
    val messages: List<MilkyOutgoingForwardedMessageData>,
) : MilkyOutgoingData()