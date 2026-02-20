package org.ntqqrev.saltify.core

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.coroutines.CoroutineContext

internal class SaltifyExceptionHandlerProvider {
    val exceptionFlow: MutableSharedFlow<Pair<CoroutineContext, Throwable>> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 16)

    val handler = CoroutineExceptionHandler { context, throwable ->
        exceptionFlow.tryEmit(context to throwable)
    }
}
