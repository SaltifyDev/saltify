package org.ntqqrev.saltify

import kotlinx.coroutines.flow.MutableSharedFlow
import org.ntqqrev.saltify.event.Event

interface ContextFactory<T> {
    /**
     * Creates a context using the given initialization config.
     * All async operations should be done in the given coroutine context.
     * The context may push events to the given channel.
     *
     * This function should not "start" the context.
     */
    suspend fun createContext(
        init: T,
        env: Environment,
        flow: MutableSharedFlow<Event>
    ): Context
}