package org.ntqqrev.saltify.message.incoming

val PrivateIncomingMessage.isSelf get() = sender == peer

suspend fun GroupIncomingMessage.isMentioningSelf(): Boolean {
    val selfUin = ctx.getLoginInfo().first
    for (segment in segments) {
        if (segment is MentionSegment && segment.uin == selfUin) {
            return true
        }
    }
    return false
}