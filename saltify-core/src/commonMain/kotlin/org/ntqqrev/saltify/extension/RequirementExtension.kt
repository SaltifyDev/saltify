package org.ntqqrev.saltify.extension

import org.ntqqrev.milky.IncomingMessage
import org.ntqqrev.saltify.entity.SaltifyCommandRequirementContext
import org.ntqqrev.saltify.model.CommandRequirement
import org.ntqqrev.saltify.model.PermissionLevel

public fun SaltifyCommandRequirementContext.user(vararg targetId: Long): CommandRequirement =
    CommandRequirement {
        context.event.senderId in targetId
    }

public fun SaltifyCommandRequirementContext.group(vararg targetId: Long): CommandRequirement =
    CommandRequirement {
        ((context.event.data as? IncomingMessage.Group)?.group?.groupId ?: return@CommandRequirement false) in targetId
    }

public fun SaltifyCommandRequirementContext.perm(targetLevel: PermissionLevel): CommandRequirement =
    CommandRequirement {
        context.event.senderPermissionLevel >= targetLevel
    }
