package org.ntqqrev.saltify.extension

import org.ntqqrev.milky.IncomingSegment

public val IncomingSegment.plainText: String
    get() = when (this) {
        is IncomingSegment.Text -> text
        is IncomingSegment.Face -> "[表情]"
        is IncomingSegment.Image -> "[图片]"
        is IncomingSegment.Record -> "[语音]"
        is IncomingSegment.Video -> "[视频]"
        is IncomingSegment.File -> "[文件]"
        is IncomingSegment.Mention -> "@$name"
        is IncomingSegment.MentionAll -> "@全体成员"
        is IncomingSegment.Reply -> "[回复]"
        is IncomingSegment.Forward -> "[合并转发]"
        is IncomingSegment.MarketFace -> "[商城表情]"
        is IncomingSegment.LightApp -> "[小程序]"
        is IncomingSegment.Xml -> "[XML消息]"
    }

/**
 * 将当前 `List<IncomingSegment>` 强制转换为文本形式，非文本形式的 segments 将以预定义文本占位。
 */
public val List<IncomingSegment>.plainText: String
    get() = joinToString("") { it.plainText }

/**
 * 判断当前 `List<IncomingSegment>` 是否由指定的 `IncomingSegment` 组成。
 */
public inline fun <reified T : IncomingSegment> List<IncomingSegment>.consistsOf(): Boolean =
    all { it is T }
