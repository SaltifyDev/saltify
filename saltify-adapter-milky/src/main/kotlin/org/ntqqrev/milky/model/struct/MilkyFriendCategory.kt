package org.ntqqrev.milky.model.struct

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MilkyFriendCategory(
    @SerialName("category_id")
    val categoryId: Int,

    @SerialName("category_name")
    val categoryName: String,
)