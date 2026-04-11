package org.ntqqrev.saltify.extension

import org.ntqqrev.milky.IncomingMessage
import org.ntqqrev.milky.IncomingSegment
import org.ntqqrev.saltify.model.PermissionLevel
import org.ntqqrev.saltify.model.command.CommandRequirement
import org.ntqqrev.saltify.runtime.command.CommandRequirementMatch

public fun CommandRequirementMatch.user(vararg targetId: Long): CommandRequirement =
    CommandRequirement {
        context.event.senderId in targetId
    }

public fun CommandRequirementMatch.group(vararg targetId: Long): CommandRequirement =
    CommandRequirement {
        ((context.event.data as? IncomingMessage.Group)?.group?.groupId ?: return@CommandRequirement false) in targetId
    }

public fun CommandRequirementMatch.perm(targetLevel: PermissionLevel): CommandRequirement =
    CommandRequirement {
        context.permissionLevelOf(context.event.senderId) >= targetLevel
    }

public val CommandRequirementMatch.isGroupAdmin: CommandRequirement
    get() = CommandRequirement {
        val data = context.event.data as? IncomingMessage.Group
        data?.groupMember?.role == "admin"
    }

public val CommandRequirementMatch.isGroupOwner: CommandRequirement
    get() = CommandRequirement {
        val data = context.event.data as? IncomingMessage.Group
        data?.groupMember?.role == "owner"
    }

public val CommandRequirementMatch.isGroupAdminOrOwner: CommandRequirement
    get() = isGroupAdmin or isGroupOwner

public val CommandRequirementMatch.isMention: CommandRequirement
    get() = CommandRequirement {
        val segment = context.event.segments.filterIsInstance<IncomingSegment.Mention>()
        segment.isNotEmpty() && segment.any { it.userId == context.event.senderId }
    }
