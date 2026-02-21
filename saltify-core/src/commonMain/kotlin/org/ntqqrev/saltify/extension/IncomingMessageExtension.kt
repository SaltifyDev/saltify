package org.ntqqrev.saltify.extension

import org.ntqqrev.milky.IncomingSegment

public val List<IncomingSegment>.plainText: String?
    get() = map { segment ->
        when (segment) {
            is IncomingSegment.Text -> segment.data.text
            is IncomingSegment.Face -> "[表情]"
            is IncomingSegment.Image -> "[图片]"
            is IncomingSegment.Record -> "[语音]"
            is IncomingSegment.Video -> "[视频]"
            is IncomingSegment.File -> "[文件]"
            is IncomingSegment.Mention -> "@${segment.data.name}"
            is IncomingSegment.MentionAll -> "@全体成员"
            is IncomingSegment.Reply -> "[回复]"
            is IncomingSegment.Forward -> "[合并转发]"
            is IncomingSegment.MarketFace -> "[商城表情]"
            is IncomingSegment.LightApp -> "[小程序]"
            is IncomingSegment.Xml -> "[XML消息]"
        }
    }
        .takeIf { it.isNotEmpty() }
        ?.joinToString("")
        ?.trim()
