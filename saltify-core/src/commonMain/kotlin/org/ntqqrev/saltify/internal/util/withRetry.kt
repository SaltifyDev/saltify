package org.ntqqrev.saltify.internal.util

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds

@Suppress("LongParameterList")
internal suspend fun withRetry(
    maxAttempts: Int,
    baseDelay: Long,
    maxDelay: Long,
    isEnabled: Boolean,
    onRetry: suspend (throwable: Throwable, retryCount: Int, delay: Long) -> Unit,
    onFailure: suspend (throwable: Throwable) -> Unit,
    block: suspend (resetAttempts: () -> Unit) -> Unit
) {
    var attempts = 0
    catchLoop@ while (currentCoroutineContext().isActive) {
        runCatching {
            block {
                @Suppress("AssignedValueIsNeverRead")
                attempts = 0
            }
            break@catchLoop
        }.onFailure { e ->
            if (!isEnabled) {
                onFailure(e)
                return@withRetry
            }

            when (e) {
                is CancellationException -> throw e
                else -> {
                    attempts++

                    if (maxAttempts in 0..<attempts) {
                        onFailure(e)
                        return@withRetry
                    }

                    val delay = calculateBackoff(attempts, baseDelay, maxDelay)
                    onRetry(e, attempts, delay)
                    delay(delay.milliseconds)
                }
            }
        }
    }
}

private fun calculateBackoff(
    attempt: Int,
    baseDelay: Long = 1000,
    maxDelay: Long = 30000
): Long {
    val shift = min(attempt, 30)
    val expDelay = baseDelay * (1L shl shift)

    return expDelay.coerceAtMost(maxDelay)
}
