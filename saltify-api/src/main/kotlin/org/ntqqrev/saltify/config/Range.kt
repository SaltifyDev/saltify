package org.ntqqrev.saltify.config

annotation class IntRange(val min: Int = 0, val max: Int = Int.MAX_VALUE)
annotation class LongRange(val min: Long = 0, val max: Long = Long.MAX_VALUE)
annotation class FloatRange(val min: Float = 0f, val max: Float = Float.MAX_VALUE)
annotation class DoubleRange(val min: Double = 0.0, val max: Double = Double.MAX_VALUE)