package org.ntqqrev.saltify.event

import kotlinx.datetime.Instant
import org.ntqqrev.saltify.Context
import org.ntqqrev.saltify.model.Group
import org.ntqqrev.saltify.model.GroupMember

abstract class AbstractGroupEvent(
    ctx: Context,
    time: Instant,

    /**
     * The group related to the event.
     */
    val group: Group
) : Event(ctx, time)

open class GroupAdminChangeEvent(
    ctx: Context,
    time: Instant,
    group: Group,

    /**
     * The group member who is changed to / from admin.
     */
    val member: GroupMember,

    /**
     * Whether the member is changed to admin (true) or from admin (false).
     */
    val isSet: Boolean,
) : AbstractGroupEvent(ctx, time, group)

open class GroupEssenceMessageChangeEvent(
    ctx: Context,
    time: Instant,
    group: Group,

    /**
     * The sequence number of the message that was set or removed from essence.
     */
    val sequence: Long,

    /**
     * Whether the message is set as essence (true) or removed from essence (false).
     */
    val isSet: Boolean,
) : AbstractGroupEvent(ctx, time, group)

open class GroupMemberIncreaseEvent(
    ctx: Context,
    time: Instant,
    group: Group,

    /**
     * The group member who joined the group.
     */
    val member: GroupMember,

    /**
     * The group member who invited the new member to the group, if applicable.
     */
    val operator: GroupMember? = null,

    /**
     * The group member who invited the new member to the group, if applicable.
     */
    val invitor: GroupMember? = null,
) : AbstractGroupEvent(ctx, time, group)

open class GroupMemberDecreaseEvent(
    ctx: Context,
    time: Instant,
    group: Group,

    /**
     * The group member who left the group.
     * It is no longer a member, so the param is not a GroupMember object.
     */
    val memberUin: Long,

    /**
     * The one who kicked the member out of the group, if applicable.
     * The bot may not be able to retrieve the operator.
     */
    val operator: GroupMember? = null,
) : AbstractGroupEvent(ctx, time, group)

open class GroupNameChangeEvent(
    ctx: Context,
    time: Instant,
    group: Group,

    /**
     * The new name of the group.
     */
    val newName: String,

    /**
     * The old name of the group.
     */
    val operator: GroupMember,
) : AbstractGroupEvent(ctx, time, group)

open class GroupMessageReactionEvent(
    ctx: Context,
    time: Instant,
    group: Group,

    /**
     * The sequence number of the message where the reaction was sent.
     */
    val sequence: Long,

    /**
     * The group member who sent the reaction.
     */
    val sender: GroupMember,

    /**
     * The ID of the reaction.
     */
    val reactionId: String,

    /**
     * Whether the reaction was added or removed.
     */
    val isAdd: Boolean,
) : AbstractGroupEvent(ctx, time, group)

open class GroupMuteEvent(
    ctx: Context,
    time: Instant,
    group: Group,

    /**
     * The group member who is muted. Null if the whole group is muted.
     */
    val member: GroupMember?,

    /**
     * The group member who muted the group member.
     * The bot may not be able to retrieve the operator.
     */
    val operator: GroupMember?,

    /**
     * The duration in seconds.
     * Null means unmuted, and -1 means permanently muted (only in whole mute).
     */
    val duration: Int?,
) : AbstractGroupEvent(ctx, time, group)

open class GroupNudgeEvent(
    ctx: Context,
    time: Instant,
    group: Group,

    /**
     * The group member who sent the nudge.
     */
    val sender: GroupMember,

    /**
     * The group member who received the nudge.
     */
    val receiver: GroupMember,
) : AbstractGroupEvent(ctx, time, group)

open class GroupFileUploadEvent(
    ctx: Context,
    time: Instant,
    group: Group,

    /**
     * The group member who uploaded the file.
     */
    val uploader: GroupMember,

    /**
     * The ID of the file that was uploaded.
     */
    val fileId: String,

    /**
     * The name of the file that was uploaded.
     */
    val fileName: String,

    /**
     * The size of the file that was uploaded, in bytes.
     */
    val fileSize: Long,
) : AbstractGroupEvent(ctx, time, group)