package org.ntqqrev.saltify.model

interface Friend : User {
    /**
     * Another identifier of the user, if defined.
     */
    val qid: String?

    /**
     * The remark name of the friend defined by the bot.
     */
    val remark: String?

    /**
     * The category which the friend belongs to.
     */
    val category: Int
}