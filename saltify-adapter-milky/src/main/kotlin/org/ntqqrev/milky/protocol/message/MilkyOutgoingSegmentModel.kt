package org.ntqqrev.milky.protocol.message

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

class MilkyOutgoingSegmentModel(
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
sealed class MilkyOutgoingData

class MilkyOutgoingTextData(
    val text: String,
) : MilkyOutgoingData()

class MilkyOutgoingMentionData(
    val userId: Long,
) : MilkyOutgoingData()

class MilkyOutgoingMentionAllData(
) : MilkyOutgoingData()

class MilkyOutgoingFaceData(
    val faceId: String,
) : MilkyOutgoingData()

class MilkyOutgoingReplyData(
    val messageSeq: Long,
) : MilkyOutgoingData()

class MilkyOutgoingImageData(
    val uri: String,
    val summary: String? = null,
    val subType: String? = null,
) : MilkyOutgoingData()

class MilkyOutgoingRecordData(
    val uri: String,
) : MilkyOutgoingData()

class MilkyOutgoingVideoData(
    val uri: String,
    val thumbUri: String? = null,
) : MilkyOutgoingData()

class MilkyOutgoingForwardData(
    val messages: List<MilkyOutgoingForwardedMessageData>,
) : MilkyOutgoingData()