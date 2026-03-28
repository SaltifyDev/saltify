package org.ntqqrev.saltify.extension

import org.ntqqrev.saltify.entity.SaltifyBotConfig
import org.ntqqrev.saltify.model.PermissionLevel

/**
 * 获取用户的权限等级。
 */
public fun permissionLevelOf(targetId: Long): PermissionLevel = when (targetId) {
    in SaltifyBotConfig.superUsers -> PermissionLevel.SuperUser
    in SaltifyBotConfig.restrictedUsers -> PermissionLevel.Restricted
    else -> PermissionLevel.Everyone
}
