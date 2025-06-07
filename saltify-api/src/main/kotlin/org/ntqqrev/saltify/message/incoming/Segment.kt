package org.ntqqrev.saltify.message.incoming

import org.ntqqrev.saltify.message.ImageSubType

/**
 * Represents part of the content of a message.
 */
abstract class Segment(
    /**
     * The message that this segment belongs to.
     */
    val message: IncomingMessage
)

/**
 * Represents a segment that contains a resource, such as an image, audio, or video.
 */
abstract class ResourceLikeSegment(
    message: IncomingMessage,

    /**
     * The permanent ID of the resource. Can be used to download the resource.
     */
    val resourceId: String
) : Segment(message)

/**
 * A segment that contains text content.
 */
open class TextSegment(
    message: IncomingMessage,

    /**
     * The text content of the segment.
     */
    val text: String
) : Segment(message)

/**
 * A segment that contains a mention of a user or all members of a group.
 */
open class MentionSegment(
    message: IncomingMessage,

    /**
     * The uin of the mentioned user. `null` means all members of the group are mentioned.
     */
    val uin: Long?
) : Segment(message)

/**
 * A segment that contains an inline face.
 */
open class FaceSegment(
    message: IncomingMessage,

    /**
     * The ID of the face.
     */
    val id: String
) : Segment(message)

/**
 * A segment that contains a reply to another message.
 */
open class ReplySegment(
    message: IncomingMessage,

    /**
     * The sequence of the message being replied to.
     */
    val repliedSequence: Long,
) : Segment(message)

/**
 * A segment that contains an image.
 */
open class ImageSegment(
    message: IncomingMessage,
    resourceId: String,

    /**
     * How the image appears in the chat window.
     */
    val subType: ImageSubType,

    /**
     * The preview text of the image.
     */
    val summary: String,
) : ResourceLikeSegment(message, resourceId)

/**
 * A segment that contains an audio recording.
 */
open class RecordSegment(
    message: IncomingMessage,
    resourceId: String,

    /**
     * The duration of the audio in seconds.
     */
    val duration: Int
) : ResourceLikeSegment(message, resourceId)

/**
 * A segment that contains a video.
 */
open class VideoSegment(
    message: IncomingMessage,
    resourceId: String,
) : ResourceLikeSegment(message, resourceId)

/**
 * A segment that contains a reference to forwarded messages.
 */
open class ForwardSegment(
    message: IncomingMessage,

    /**
     * The ID of the forwarded messages.
     * Can be used to retrieve the original messages.
     */
    val forwardId: String
) : Segment(message)

/**
 * A segment that contains a market face, which is a special type
 * of face that can be downloaded from a market.
 */
open class MarketFaceSegment(
    message: IncomingMessage,

    /**
     * The URL of the market face.
     */
    val url: String
) : Segment(message)

/**
 * A segment that contains a light app, which is a mini application
 * that can be run within the messaging platform.
 * This is a fallback for the light apps that do not match the
 * built-in light app types.
 */
open class LightAppSegment(
    message: IncomingMessage,

    /**
     * The name of the light app.
     */
    val appName: String,

    /**
     * The JSON payload of the light app.
     */
    val jsonPayload: String
) : Segment(message)

/**
 * A segment that contains XML data.
 */
open class XmlSegment(
    message: IncomingMessage,

    /**
     * The service ID of the XML segment.
     */
    val serviceId: Int,

    /**
     * The XML payload.
     */
    val xmlPayload: String
) : Segment(message)