package org.ntqqrev.saltify.message.outgoing

import org.ntqqrev.saltify.message.ImageSubType
import org.ntqqrev.saltify.message.incoming.IncomingMessage
import java.io.InputStream

interface TextFeature {
    /**
     * Create a text segment.
     */
    fun text(text: String)
}

interface MentionFeature {
    /**
     * Mention a user in the group.
     */
    fun mention(uin: Long)

    /**
     * Mention all users in the group.
     */
    fun mentionAll()
}

interface FaceFeature {
    /**
     * Create a face segment with the given face ID.
     */
    fun face(id: String)
}

interface ReplyFeature {
    /**
     * Reply to a message.
     */
    fun reply(sequence: Long)
}

interface ImageFeature {
    /**
     * Create an image segment.
     */
    fun image(resource: ResourceLocation, subType: ImageSubType = ImageSubType.NORMAL, summary: String? = "")
}

interface RecordFeature {
    /**
     * Create a record segment.
     */
    fun record(resource: ResourceLocation)
}

interface VideoFeature {
    /**
     * Create a video segment with custom cover (if provided).
     */
    fun video(resource: ResourceLocation, cover: InputStream? = null)
}

interface ForwardFeature {
    /**
     * Create a forward segment with multiple messages.
     */
    fun forward(packer: Packer.() -> Unit)

    interface Packer {
        /**
         * Construct a forwarded message with the given uin and name.
         */
        fun fake(uin: Long, name: String, builder: ForwardMessageBuilder.() -> Unit)

        /**
         * Forward an existing message.
         */
        fun existing(incomingMessage: IncomingMessage)
    }
}