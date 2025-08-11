package org.ntqqrev.milky.entity

import org.ntqqrev.saltify.Context
import org.ntqqrev.saltify.model.Gender
import org.ntqqrev.saltify.model.User

class MilkyStranger(
    override val ctx: Context,
    override val uin: Long,
    override val nickname: String,
    override val gender: Gender,
) : User