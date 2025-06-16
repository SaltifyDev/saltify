package org.ntqqrev.saltify.message.incoming

import org.ntqqrev.saltify.Context
import org.ntqqrev.saltify.Entity
import org.ntqqrev.saltify.message.ImageSubType

/**
 * Represents part of the content of a message.
 */
abstract class Segment(
    override val ctx: Context
) : Entity

/**
 * Represents a segment that contains a resource, such as an image, audio, or video.
 */
abstract class ResourceLikeSegment(
    ctx: Context,

    /**
     * The permanent ID of the resource. Can be used to download the resource.
     */
    val resourceId: String
) : Segment(ctx)

/**
 * A segment that contains text content.
 */
open class TextSegment(
    ctx: Context,

    /**
     * The text content of the segment.
     */
    val text: String
) : Segment(ctx)

/**
 * A segment that contains a mention of a user or all members of a group.
 */
open class MentionSegment(
    ctx: Context,

    /**
     * The uin of the mentioned user. `null` means all members of the group are mentioned.
     */
    val uin: Long?
) : Segment(ctx)

/**
 * A segment that contains an inline face.
 */
open class FaceSegment(
    ctx: Context,

    /**
     * The ID of the face.
     */
    val id: String
) : Segment(ctx)

/**
 * A segment that contains a reply to another message.
 */
open class ReplySegment(
    ctx: Context,

    /**
     * The sequence of the message being replied to.
     */
    val repliedSequence: Long,
) : Segment(ctx)

/**
 * A segment that contains an image.
 */
open class ImageSegment(
    ctx: Context,
    resourceId: String,

    /**
     * How the image appears in the chat window.
     */
    val subType: ImageSubType,

    /**
     * The preview text of the image.
     */
    val summary: String,
) : ResourceLikeSegment(ctx, resourceId)

/**
 * A segment that contains an audio recording.
 */
open class RecordSegment(
    ctx: Context,
    resourceId: String,

    /**
     * The duration of the audio in seconds.
     */
    val duration: Int
) : ResourceLikeSegment(ctx, resourceId)

/**
 * A segment that contains a video.
 */
open class VideoSegment(
    ctx: Context,
    resourceId: String,
) : ResourceLikeSegment(ctx, resourceId)

/**
 * A segment that contains a reference to forwarded messages.
 */
open class ForwardSegment(
    ctx: Context,

    /**
     * The ID of the forwarded messages.
     * Can be used to retrieve the original messages.
     */
    val forwardId: String
) : Segment(ctx)

/**
 * A segment that contains a market face, which is a special type
 * of face that can be downloaded from a market.
 */
open class MarketFaceSegment(
    ctx: Context,

    /**
     * The URL of the market face.
     */
    val url: String
) : Segment(ctx)

/**
 * A segment that contains a light app, which is a mini application
 * that can be run within the messaging platform.
 * This is a fallback for the light apps that do not match the
 * built-in light app types.
 */
open class LightAppSegment(
    ctx: Context,

    /**
     * The name of the light app.
     */
    val appName: String,

    /**
     * The JSON payload of the light app.
     */
    val jsonPayload: String
) : Segment(ctx)

/**
 * A segment that contains XML data.
 */
open class XmlSegment(
    ctx: Context,

    /**
     * The service ID of the XML segment.
     */
    val serviceId: Int,

    /**
     * The XML payload.
     */
    val xmlPayload: String
) : Segment(ctx)