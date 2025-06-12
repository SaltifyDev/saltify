package org.ntqqrev.milky.model.message

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

internal class MilkyIncomingSegment(
    val type: String,
    val data: MilkyIncomingData,
)

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
internal sealed class MilkyIncomingData

internal class MilkyIncomingTextData(
    val text: String,
) : MilkyIncomingData()

internal class MilkyIncomingMentionData(
    val userId: Long,
) : MilkyIncomingData()

internal class MilkyIncomingMentionAllData(
) : MilkyIncomingData()

internal class MilkyIncomingFaceData(
    val faceId: String,
) : MilkyIncomingData()

internal class MilkyIncomingReplyData(
    val messageSeq: Long,
) : MilkyIncomingData()

internal class MilkyIncomingImageData(
    val resourceId: String,
    val tempUrl: String,
    val summary: String? = null,
    val subType: String? = null,
) : MilkyIncomingData()

internal class MilkyIncomingRecordData(
    val resourceId: String,
    val tempUrl: String,
    val duration: Int,
) : MilkyIncomingData()

internal class MilkyIncomingVideoData(
    val resourceId: String,
    val tempUrl: String,
) : MilkyIncomingData()

internal class MilkyIncomingForwardData(
    val forwardId: String,
) : MilkyIncomingData()

internal class MilkyIncomingMarketFaceData(
    val url: String,
) : MilkyIncomingData()

internal class MilkyIncomingLightAppData(
    val appName: String,
    val jsonPayload: String,
) : MilkyIncomingData()

internal class MilkyIncomingXmlData(
    val serviceId: Int,
    val xmlPayload: String,
) : MilkyIncomingData()