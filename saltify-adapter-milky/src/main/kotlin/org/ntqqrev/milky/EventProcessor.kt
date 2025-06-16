package org.ntqqrev.milky

import kotlinx.datetime.Instant
import org.ntqqrev.milky.protocol.event.MilkyBotOfflineEvent
import org.ntqqrev.milky.protocol.event.MilkyEvent
import org.ntqqrev.milky.protocol.event.MilkyFriendFileUploadEvent
import org.ntqqrev.milky.protocol.event.MilkyFriendNudgeEvent
import org.ntqqrev.milky.protocol.event.MilkyGroupAdminChangeEvent
import org.ntqqrev.milky.protocol.event.MilkyGroupEssenceMessageChangeEvent
import org.ntqqrev.milky.protocol.event.MilkyGroupFileUploadEvent
import org.ntqqrev.milky.protocol.event.MilkyGroupMemberDecreaseEvent
import org.ntqqrev.milky.protocol.event.MilkyGroupMemberIncreaseEvent
import org.ntqqrev.milky.protocol.event.MilkyGroupMessageReactionEvent
import org.ntqqrev.milky.protocol.event.MilkyGroupMuteEvent
import org.ntqqrev.milky.protocol.event.MilkyGroupNameChangeEvent
import org.ntqqrev.milky.protocol.event.MilkyGroupNudgeEvent
import org.ntqqrev.milky.protocol.event.MilkyGroupWholeMuteEvent
import org.ntqqrev.milky.protocol.event.MilkyMessageRecallEvent
import org.ntqqrev.milky.protocol.message.MilkyIncomingMessageData
import org.ntqqrev.milky.protocol.request.MilkyFriendRequestData
import org.ntqqrev.milky.protocol.request.MilkyGroupInvitationData
import org.ntqqrev.milky.protocol.request.MilkyGroupInviteRequestData
import org.ntqqrev.milky.protocol.request.MilkyGroupJoinRequestData
import org.ntqqrev.milky.util.toSaltifyMessageScene
import org.ntqqrev.milky.util.toSaltifyRequestState
import org.ntqqrev.saltify.Context
import org.ntqqrev.saltify.event.FriendFileUploadEvent
import org.ntqqrev.saltify.event.FriendNudgeEvent
import org.ntqqrev.saltify.event.FriendRequestEvent
import org.ntqqrev.saltify.event.GroupAdminChangeEvent
import org.ntqqrev.saltify.event.GroupEssenceMessageChangeEvent
import org.ntqqrev.saltify.event.GroupFileUploadEvent
import org.ntqqrev.saltify.event.GroupInvitationEvent
import org.ntqqrev.saltify.event.GroupInvitedJoinRequestEvent
import org.ntqqrev.saltify.event.GroupJoinRequestEvent
import org.ntqqrev.saltify.event.GroupMemberDecreaseEvent
import org.ntqqrev.saltify.event.GroupMemberIncreaseEvent
import org.ntqqrev.saltify.event.GroupMessageReactionEvent
import org.ntqqrev.saltify.event.GroupMuteEvent
import org.ntqqrev.saltify.event.GroupNameChangeEvent
import org.ntqqrev.saltify.event.GroupNudgeEvent
import org.ntqqrev.saltify.event.MessageRecallEvent
import org.ntqqrev.saltify.event.MessageReceiveEvent
import org.ntqqrev.saltify.getMember

internal suspend fun MilkyContext.processEvent(event: MilkyEvent) {
    val data = event.data
    when (data) {
        is MilkyBotOfflineEvent -> {
            instanceState = Context.State.INTERRUPTED
        }

        is MilkyIncomingMessageData -> {
            val message = data.toSaltifyMessage()
            flow.emit(
                MessageReceiveEvent(
                    ctx = this,
                    message = message
                )
            )
        }

        is MilkyMessageRecallEvent -> {
            flow.emit(
                MessageRecallEvent(
                    ctx = this,
                    time = Instant.fromEpochSeconds(event.time),
                    messageScene = data.messageScene.toSaltifyMessageScene(),
                    peerUin = data.peerId,
                    sequence = data.messageSeq,
                    operator = data.operatorId?.let {
                        getGroupMember(data.peerId, it)
                    }
                )
            )
        }

        is MilkyFriendRequestData -> {
            flow.emit(
                FriendRequestEvent(
                    ctx = this,
                    time = Instant.fromEpochSeconds(event.time),
                    requestId = data.requestId,
                    isFiltered = data.isFiltered,
                    initiatorUin = data.initiatorId,
                    state = data.state.toSaltifyRequestState(),
                    comment = data.comment ?: "",
                    via = data.via ?: ""
                )
            )
        }

        is MilkyGroupJoinRequestData -> {
            flow.emit(
                GroupJoinRequestEvent(
                    ctx = this,
                    time = Instant.fromEpochSeconds(event.time),
                    requestId = data.requestId,
                    isFiltered = data.isFiltered,
                    initiatorUin = data.initiatorId,
                    state = data.state.toSaltifyRequestState(),
                    groupUin = data.groupId,
                    operatorUin = data.operatorId,
                    comment = data.comment ?: "",
                )
            )
        }

        is MilkyGroupInviteRequestData -> {
            flow.emit(
                GroupInvitedJoinRequestEvent(
                    ctx = this,
                    time = Instant.fromEpochSeconds(event.time),
                    requestId = data.requestId,
                    isFiltered = data.isFiltered,
                    initiatorUin = data.initiatorId,
                    state = data.state.toSaltifyRequestState(),
                    groupUin = data.groupId,
                    operatorUin = data.operatorId,
                    inviteeUin = data.inviteeId,
                )
            )
        }

        is MilkyGroupInvitationData -> {
            flow.emit(
                GroupInvitationEvent(
                    ctx = this,
                    time = Instant.fromEpochSeconds(event.time),
                    requestId = data.requestId,
                    isFiltered = data.isFiltered,
                    initiatorUin = data.initiatorId,
                    state = data.state.toSaltifyRequestState(),
                    groupUin = data.groupId
                )
            )
        }

        is MilkyFriendNudgeEvent -> {
            val friend = getFriend(data.userId)
            if (friend == null) {
                logger.warn { "Received friend nudge for unknown user ${data.userId}" }
                return
            }
            flow.emit(
                FriendNudgeEvent(
                    ctx = this,
                    time = Instant.fromEpochSeconds(event.time),
                    friend = friend,
                    isSelfSend = data.isSelfSend,
                    isSelfReceive = data.isSelfReceive
                )
            )
        }

        is MilkyFriendFileUploadEvent -> {
            val friend = getFriend(data.userId)
            if (friend == null) {
                logger.warn { "Received file upload event for unknown user ${data.userId}" }
                return
            }
            flow.emit(
                FriendFileUploadEvent(
                    ctx = this,
                    time = Instant.fromEpochSeconds(event.time),
                    friend = friend,
                    fileId = data.fileId,
                    fileName = data.fileName,
                    fileSize = data.fileSize,
                    isSelf = data.isSelf
                )
            )
        }

        is MilkyGroupAdminChangeEvent -> {
            val group = getGroup(data.groupId)
            if (group == null) {
                logger.warn { "Received group admin change event for unknown group ${data.groupId}" }
                return
            }
            val member = group.getMember(data.userId)
            if (member == null) {
                logger.warn { "Received group admin change event for unknown member ${data.userId} in group ${data.groupId}" }
                return
            }
            flow.emit(
                GroupAdminChangeEvent(
                    ctx = this,
                    time = Instant.fromEpochSeconds(event.time),
                    group = group,
                    member = member,
                    isSet = data.isSet
                )
            )
        }

        is MilkyGroupEssenceMessageChangeEvent -> {
            val group = getGroup(data.groupId)
            if (group == null) {
                logger.warn { "Received group essence message change event for unknown group ${data.groupId}" }
                return
            }
            flow.emit(
                GroupEssenceMessageChangeEvent(
                    ctx = this,
                    time = Instant.fromEpochSeconds(event.time),
                    group = group,
                    sequence = data.messageSeq,
                    isSet = data.isSet
                )
            )
        }

        is MilkyGroupMemberIncreaseEvent -> {
            val group = getGroup(data.groupId)
            if (group == null) {
                logger.warn { "Received group member increase event for unknown group ${data.groupId}" }
                return
            }
            val member = group.getMember(data.userId)
            if (member == null) {
                logger.warn { "Received group member increase event for unknown member ${data.userId} in group ${data.groupId}" }
                return
            }
            flow.emit(
                GroupMemberIncreaseEvent(
                    ctx = this,
                    time = Instant.fromEpochSeconds(event.time),
                    group = group,
                    member = member,
                    operator = data.operatorId?.let { group.getMember(it) },
                    invitor = data.invitorId?.let { group.getMember(it) }
                )
            )
        }

        is MilkyGroupMemberDecreaseEvent -> {
            val group = getGroup(data.groupId)
            if (group == null) {
                logger.warn { "Received group member decrease event for unknown group ${data.groupId}" }
                return
            }
            group.groupMemberCache.updatePreventRepeated()
            flow.emit(
                GroupMemberDecreaseEvent(
                    ctx = this,
                    time = Instant.fromEpochSeconds(event.time),
                    group = group,
                    memberUin = data.userId,
                    operator = data.operatorId?.let { group.getMember(it) }
                )
            )
        }

        is MilkyGroupNameChangeEvent -> {
            val group = getGroup(data.groupId)
            if (group == null) {
                logger.warn { "Received group name change event for unknown group ${data.groupId}" }
                return
            }
            val member = group.getMember(data.operatorId)
            if (member == null) {
                logger.warn { "Received group name change event for unknown member ${data.operatorId} in group ${data.groupId}" }
                return
            }
            flow.emit(
                GroupNameChangeEvent(
                    ctx = this,
                    time = Instant.fromEpochSeconds(event.time),
                    group = group,
                    newName = data.name,
                    operator = member,
                )
            )
        }

        is MilkyGroupMessageReactionEvent -> {
            val group = getGroup(data.groupId)
            if (group == null) {
                logger.warn { "Received group message reaction event for unknown group ${data.groupId}" }
                return
            }
            val member = group.getMember(data.userId)
            if (member == null) {
                logger.warn { "Received group message reaction event for unknown member ${data.userId} in group ${data.groupId}" }
                return
            }
            flow.emit(
                GroupMessageReactionEvent(
                    ctx = this,
                    time = Instant.fromEpochSeconds(event.time),
                    group = group,
                    sequence = data.messageSeq,
                    sender = member,
                    reactionId = data.faceId,
                    isAdd = data.isAdd
                )
            )
        }

        is MilkyGroupMuteEvent -> {
            val group = getGroup(data.groupId)
            if (group == null) {
                logger.warn { "Received group mute event for unknown group ${data.groupId}" }
                return
            }
            val member = group.getMember(data.userId)
            if (member == null) {
                logger.warn { "Received group mute event for unknown member ${data.userId} in group ${data.groupId}" }
                return
            }
            val operator = group.getMember(data.operatorId)
            if (operator == null) {
                logger.warn { "Received group mute event for unknown operator ${data.operatorId} in group ${data.groupId}" }
            }
            flow.emit(
                GroupMuteEvent(
                    ctx = this,
                    time = Instant.fromEpochSeconds(event.time),
                    group = group,
                    member = member,
                    duration = data.duration,
                    operator = operator
                )
            )
        }

        is MilkyGroupWholeMuteEvent -> {
            val group = getGroup(data.groupId)
            if (group == null) {
                logger.warn { "Received group whole mute event for unknown group ${data.groupId}" }
                return
            }
            val operator = group.getMember(data.operatorId)
            if (operator == null) {
                logger.warn { "Received group whole mute event for unknown operator ${data.operatorId} in group ${data.groupId}" }
            }
            flow.emit(
                GroupMuteEvent(
                    ctx = this,
                    time = Instant.fromEpochSeconds(event.time),
                    group = group,
                    member = null,
                    duration = if (data.isMute) -1 else 0,
                    operator = operator
                )
            )
        }

        is MilkyGroupNudgeEvent -> {
            val group = getGroup(data.groupId)
            if (group == null) {
                logger.warn { "Received group nudge event for unknown group ${data.groupId}" }
                return
            }
            val sender = group.getMember(data.senderId)
            if (sender == null) {
                logger.warn { "Received group nudge event for unknown sender ${data.senderId} in group ${data.groupId}" }
                return
            }
            val receiver = group.getMember(data.receiverId)
            if (receiver == null) {
                logger.warn { "Received group nudge event for unknown receiver ${data.receiverId} in group ${data.groupId}" }
                return
            }
            flow.emit(
                GroupNudgeEvent(
                    ctx = this,
                    time = Instant.fromEpochSeconds(event.time),
                    group = group,
                    sender = sender,
                    receiver = receiver
                )
            )
        }

        is MilkyGroupFileUploadEvent -> {
            val group = getGroup(data.groupId)
            if (group == null) {
                logger.warn { "Received group file upload event for unknown group ${data.groupId}" }
                return
            }
            val member = group.getMember(data.userId)
            if (member == null) {
                logger.warn { "Received group file upload event for unknown member ${data.userId} in group ${data.groupId}" }
                return
            }
            flow.emit(
                GroupFileUploadEvent(
                    ctx = this,
                    time = Instant.fromEpochSeconds(event.time),
                    group = group,
                    uploader = member,
                    fileId = data.fileId,
                    fileName = data.fileName,
                    fileSize = data.fileSize
                )
            )
        }

        else -> {
            logger.warn { "Received unsupported event type: ${data::class.simpleName}" }
        }
    }
}