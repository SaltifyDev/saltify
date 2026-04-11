package org.ntqqrev.saltify.dsl.config

import org.ntqqrev.saltify.annotation.SaltifyDsl

@SaltifyDsl
public class BotConfig {
    /**
     * 默认指令前缀。
     */
    public var commandPrefix: String = "/"

    /**
     * 最高权限用户列表。
     */
    public var superUsers: MutableSet<Long> = mutableSetOf()

    /**
     * 受限用户列表。
     */
    public var restrictedUsers: MutableSet<Long> = mutableSetOf()
}
