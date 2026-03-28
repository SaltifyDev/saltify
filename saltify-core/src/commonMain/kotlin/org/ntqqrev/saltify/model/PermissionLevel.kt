package org.ntqqrev.saltify.model

@Suppress("MagicNumber")
public open class PermissionLevel(
    public val weight: Int,
) : Comparable<PermissionLevel> {
    override fun compareTo(other: PermissionLevel): Int = this.weight.compareTo(other.weight)

    /**
     * 受限制
     */
    public object Restricted : PermissionLevel(Int.MIN_VALUE)

    /**
     * 所有人
     */
    public object Everyone : PermissionLevel(0)

    /**
     * 超级用户
     */
    public object SuperUser : PermissionLevel(Int.MAX_VALUE)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PermissionLevel) return false
        return weight == other.weight
    }

    override fun hashCode(): Int = weight

    override fun toString(): String = "PermissionLevel(weight=$weight)"
}
