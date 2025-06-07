package org.ntqqrev.lagrange.internal.util.binary

internal open class Prefix(val value: Int) {
    data object NONE : Prefix(0b0000)

    data object UINT_8 : Prefix(0b0010)

    data object UINT_16 : Prefix(0b0100)

    data object UINT_32 : Prefix(0b1000)

    data object INCLUDE_PREFIX : Prefix(0b0001)

    data object LENGTH_ONLY : Prefix(0b0000)

    companion object {
        fun values(): Array<Prefix> = arrayOf(NONE, UINT_8, UINT_16, UINT_32, INCLUDE_PREFIX, LENGTH_ONLY)

        fun valueOf(value: String): Prefix = when (value) {
            "NONE" -> NONE
            "UINT_8" -> UINT_8
            "UINT_16" -> UINT_16
            "UINT_32" -> UINT_32
            "INCLUDE_PREFIX" -> INCLUDE_PREFIX
            "LENGTH_ONLY" -> LENGTH_ONLY
            else -> throw IllegalArgumentException("No object org.lagrange.dev.utils.ext.Prefix.$value")
        }
    }

    fun getPrefixLength(): Int = (this.value and 0b1110) shr 1

    fun isIncludePrefix(): Boolean = (this.value and 0b0001) == 1

    infix fun or(other: Prefix): Prefix = Prefix(this.value or other.value)
}