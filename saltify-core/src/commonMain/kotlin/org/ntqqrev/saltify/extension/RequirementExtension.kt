package org.ntqqrev.saltify.extension

import org.ntqqrev.milky.IncomingMessage
import org.ntqqrev.milky.IncomingSegment
import org.ntqqrev.saltify.entity.CommandRequirementContext
import org.ntqqrev.saltify.model.CommandRequirement
import org.ntqqrev.saltify.model.PermissionLevel

public fun CommandRequirementContext.user(vararg targetId: Long): CommandRequirement =
    CommandRequirement {
        context.event.senderId in targetId
    }

public fun CommandRequirementContext.group(vararg targetId: Long): CommandRequirement =
    CommandRequirement {
        ((context.event.data as? IncomingMessage.Group)?.group?.groupId ?: return@CommandRequirement false) in targetId
    }

public fun CommandRequirementContext.perm(targetLevel: PermissionLevel): CommandRequirement =
    CommandRequirement {
        permissionLevelOf(context.event.senderId) >= targetLevel
    }

public val CommandRequirementContext.isGroupAdmin: CommandRequirement
    get() = CommandRequirement {
        val data = context.event.data as? IncomingMessage.Group
        data?.groupMember?.role == "admin"
    }

public val CommandRequirementContext.isGroupOwner: CommandRequirement
    get() = CommandRequirement {
        val data = context.event.data as? IncomingMessage.Group
        data?.groupMember?.role == "owner"
    }

public val CommandRequirementContext.isGroupAdminOrOwner: CommandRequirement
    get() = isGroupAdmin or isGroupOwner

public val CommandRequirementContext.isMention: CommandRequirement
    get() = CommandRequirement {
        val segment = context.event.segments.filterIsInstance<IncomingSegment.Mention>()
        segment.isNotEmpty() && segment.any { it.userId == context.event.senderId }
    }
