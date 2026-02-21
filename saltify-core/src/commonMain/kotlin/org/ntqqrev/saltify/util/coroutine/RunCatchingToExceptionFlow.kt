package org.ntqqrev.saltify.util.coroutine

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import org.ntqqrev.saltify.core.SaltifyApplication
import kotlin.coroutines.CoroutineContext

/**
 * 执行代码块，并将可能的异常发送到全局异常流。
 */
public suspend inline fun SaltifyApplication.runCatchingToExceptionFlow(
    context: CoroutineContext? = null,
    crossinline block: suspend () -> Unit
) {
    runCatching {
        block()
    }.onFailure { throwable ->
        if (throwable is CancellationException) throw throwable
        exceptionHandlerProvider.exceptionFlow.tryEmit((context ?: currentCoroutineContext()) to throwable)
    }
}
