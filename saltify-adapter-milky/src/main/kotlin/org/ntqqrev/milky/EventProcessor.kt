package org.ntqqrev.milky

import kotlinx.datetime.Instant
import org.ntqqrev.milky.entity.MilkyStranger
import org.ntqqrev.milky.message.MilkyIncomingMessage
import org.ntqqrev.milky.protocol.event.*
import org.ntqqrev.milky.protocol.message.MilkyIncomingMessageData
import org.ntqqrev.milky.protocol.request.MilkyFriendRequestData
import org.ntqqrev.milky.protocol.request.MilkyGroupInvitationData
import org.ntqqrev.milky.protocol.request.MilkyGroupRequestData
import org.ntqqrev.milky.util.toEvent
import org.ntqqrev.milky.util.toSaltifyMessageScene
import org.ntqqrev.saltify.Context
import org.ntqqrev.saltify.event.*
import org.ntqqrev.saltify.getMember
import org.ntqqrev.saltify.message.MessageScene
import org.ntqqrev.saltify.model.Gender

internal suspend fun MilkyContext.processEvent(event: MilkyEvent) {
    when (val data = event.data) {
        is MilkyBotOfflineEvent -> {
            instanceState = Context.State.INTERRUPTED
        }

        is MilkyIncomingMessageData -> {
            val message = MilkyIncomingMessage.fromData(this, data)
            when (message.scene) {
                MessageScene.FRIEND -> {
                    val friend = getFriend(message.peerUin)
                    if (friend == null) {
                        logger.warn { "Received message from unknown friend ${message.peerUin}" }
                        return
                    }
                    flow.emit(FriendMessageReceiveEvent(this, message, friend))
                }

                MessageScene.GROUP -> {
                    val group = getGroup(message.peerUin)
                    if (group == null) {
                        logger.warn { "Received message from unknown group ${message.peerUin}" }
                        return
                    }
                    val member = group.getMember(message.senderUin)
                    if (member == null) {
                        logger.warn { "Received message from unknown member ${message.senderUin} in group ${message.peerUin}" }
                        return
                    }
                    flow.emit(GroupMessageReceiveEvent(this, message, group, member))
                }

                MessageScene.TEMP -> {
                    val group = getGroup(message.peerUin)
                    val sender = group?.getMember(message.senderUin) ?: MilkyStranger(
                        this,
                        message.senderUin,
                        message.senderName,
                        Gender.UNKNOWN
                    )
                    flow.emit(TempMessageReceiveEvent(this, message, sender, group))
                }
            }
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
            flow.emit(data.toEvent(this))
        }

        is MilkyGroupRequestData -> {
            flow.emit(data.toEvent(this))
        }

        is MilkyGroupInvitationData -> {
            flow.emit(data.toEvent(this))
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
                    newName = data.newGroupName,
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