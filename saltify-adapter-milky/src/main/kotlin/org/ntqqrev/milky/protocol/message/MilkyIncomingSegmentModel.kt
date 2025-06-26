package org.ntqqrev.milky.protocol.message

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

class MilkyIncomingSegmentModel(
    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "type",
    )
    @JsonSubTypes(
        JsonSubTypes.Type(MilkyIncomingTextData::class, "text"),
        JsonSubTypes.Type(MilkyIncomingMentionData::class, "mention"),
        JsonSubTypes.Type(MilkyIncomingMentionAllData::class, "mention_all"),
        JsonSubTypes.Type(MilkyIncomingFaceData::class, "face"),
        JsonSubTypes.Type(MilkyIncomingReplyData::class, "reply"),
        JsonSubTypes.Type(MilkyIncomingImageData::class, "image"),
        JsonSubTypes.Type(MilkyIncomingRecordData::class, "record"),
        JsonSubTypes.Type(MilkyIncomingVideoData::class, "video"),
        JsonSubTypes.Type(MilkyIncomingForwardData::class, "forward"),
        JsonSubTypes.Type(MilkyIncomingMarketFaceData::class, "market_face"),
        JsonSubTypes.Type(MilkyIncomingLightAppData::class, "light_app"),
        JsonSubTypes.Type(MilkyIncomingXmlData::class, "xml"),
    )
    val data: MilkyIncomingData,
)

sealed class MilkyIncomingData

class MilkyIncomingTextData(
    val text: String,
) : MilkyIncomingData()

class MilkyIncomingMentionData(
    val userId: Long,
) : MilkyIncomingData()

class MilkyIncomingMentionAllData(
) : MilkyIncomingData()

class MilkyIncomingFaceData(
    val faceId: String,
) : MilkyIncomingData()

class MilkyIncomingReplyData(
    val messageSeq: Long,
) : MilkyIncomingData()

class MilkyIncomingImageData(
    val resourceId: String,
    val tempUrl: String,
    val summary: String? = null,
    val subType: String? = null,
) : MilkyIncomingData()

class MilkyIncomingRecordData(
    val resourceId: String,
    val tempUrl: String,
    val duration: Int,
) : MilkyIncomingData()

class MilkyIncomingVideoData(
    val resourceId: String,
    val tempUrl: String,
) : MilkyIncomingData()

class MilkyIncomingForwardData(
    val forwardId: String,
) : MilkyIncomingData()

class MilkyIncomingMarketFaceData(
    val url: String,
) : MilkyIncomingData()

class MilkyIncomingLightAppData(
    val appName: String,
    val jsonPayload: String,
) : MilkyIncomingData()

class MilkyIncomingXmlData(
    val serviceId: Int,
    val xmlPayload: String,
) : MilkyIncomingData()