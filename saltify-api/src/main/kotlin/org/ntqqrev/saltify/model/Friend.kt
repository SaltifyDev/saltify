package org.ntqqrev.saltify.model

interface Friend : User {
    /**
     * The remark name of the friend defined by the bot.
     */
    val remark: String?

    /**
     * The category which the friend belongs to.
     */
    val category: Int
}