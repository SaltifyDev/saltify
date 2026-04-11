package org.ntqqrev.saltify.extension

import org.ntqqrev.saltify.runtime.context.ApplicationContext
import org.ntqqrev.saltify.model.PermissionLevel

/**
 * 获取用户的权限等级。
 */
public fun ApplicationContext.permissionLevelOf(targetId: Long): PermissionLevel = when (targetId) {
    in client.config.bot.superUsers -> PermissionLevel.SuperUser
    in client.config.bot.restrictedUsers -> PermissionLevel.Restricted
    else -> PermissionLevel.Everyone
}
