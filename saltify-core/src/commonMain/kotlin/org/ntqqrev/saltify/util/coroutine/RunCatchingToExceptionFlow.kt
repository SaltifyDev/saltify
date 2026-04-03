package org.ntqqrev.saltify.util.coroutine

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import org.ntqqrev.saltify.context.ApplicationExecutionContext
import org.ntqqrev.saltify.context.client
import kotlin.coroutines.CoroutineContext

/**
 * 执行代码块，并将可能的异常发送到全局异常流。
 */
context(_: ApplicationExecutionContext)
public suspend inline fun runCatchingToExceptionFlow(
    context: CoroutineContext? = null,
    crossinline block: suspend () -> Unit
) {
    runCatching {
        block()
    }.onFailure { throwable ->
        if (throwable is CancellationException) throw throwable
        client.exceptionHandlerProvider.exceptionFlow.tryEmit((context ?: currentCoroutineContext()) to throwable)
    }
}
