package org.ntqqrev.saltify.message.incoming

suspend fun IncomingMessage.isMentioningSelf(): Boolean {
    val selfUin = ctx.getLoginInfo().first
    for (segment in segments) {
        if (segment is MentionSegment && segment.uin == selfUin) {
            return true
        }
    }
    return false
}