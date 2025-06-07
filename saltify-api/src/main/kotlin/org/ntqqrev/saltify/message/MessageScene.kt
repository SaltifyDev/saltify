package org.ntqqrev.saltify.message

enum class MessageScene(val value: String) {
    /**
     * The message is sent in a private chat.
     */
    FRIEND("friend"),

    /**
     * The message is sent in a group chat.
     */
    GROUP("group"),

    /**
     * The message is from a non-friend member of a group.
     */
    TEMP("temp"),
}