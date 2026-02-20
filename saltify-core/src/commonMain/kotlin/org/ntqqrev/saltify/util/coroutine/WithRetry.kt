package org.ntqqrev.saltify.util.coroutine

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.log2
import kotlin.math.min
import kotlin.math.pow

@Suppress("LongParameterList")
internal suspend fun withRetry(
    maxAttempts: Int,
    baseDelay: Long,
    maxDelay: Long,
    isEnabled: Boolean,
    onRetry: suspend (throwable: Throwable, retryCount: Int) -> Unit,
    onFailure: suspend (throwable: Throwable) -> Unit,
    block: suspend () -> Unit
) {
    var attempts = 0
    while (currentCoroutineContext().isActive) {
        runCatching {
            block()
        }.onFailure { e ->
            if (!isEnabled) {
                onFailure(e)
                return@withRetry
            }

            when (e) {
                is CancellationException -> throw e
                else -> {
                    attempts++

                    @Suppress("EmptyRange")
                    if (maxAttempts in 0..<attempts) {
                        onFailure(e)
                        return@withRetry
                    }

                    onRetry(e, attempts)

                    delay(calculateBackoff(attempts, baseDelay, maxDelay))
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
    val maxSafeAttempt = log2(maxDelay.toDouble() / baseDelay).toInt()
    val expDelay = baseDelay * 2.0.pow(min(attempt, maxSafeAttempt)).toLong()

    return expDelay.coerceAtMost(maxDelay)
}
