package org.ntqqrev.saltify.message.outgoing

import org.ntqqrev.saltify.message.incoming.GroupIncomingMessage
import org.ntqqrev.saltify.message.incoming.IncomingMessage
import org.ntqqrev.saltify.model.GroupMember

fun MentionFeature.mention(member: GroupMember) {
    mention(member.uin)
}

fun ReplyFeature.reply(message: IncomingMessage) {
    reply(message.sequence)
}

fun GroupMessageBuilder.replyAndMention(message: GroupIncomingMessage) {
    reply(message.sequence)
    mention(message.sender.uin)
}