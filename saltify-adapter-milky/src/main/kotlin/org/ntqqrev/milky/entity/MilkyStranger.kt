package org.ntqqrev.milky.entity

import org.ntqqrev.milky.MilkyContext
import org.ntqqrev.saltify.model.Gender
import org.ntqqrev.saltify.model.User

class MilkyStranger(
    override val ctx: MilkyContext,
    override val uin: Long,
    override val nickname: String,
    override val gender: Gender,
) : User