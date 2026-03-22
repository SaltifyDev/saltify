package org.ntqqrev.saltify.entity

import org.ntqqrev.saltify.annotation.SaltifyDsl

@SaltifyDsl
public object SaltifyBotConfig {
    /**
     * 默认指令前缀。
     */
    public var commandPrefix: String = "/"

    /**
     * 最高权限用户列表。
     */
    public var superUsers: MutableList<Long> = mutableListOf()

    /**
     * 受限用户列表。
     */
    public var restrictedUsers: MutableList<Long> = mutableListOf()
}
