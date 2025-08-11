package org.ntqqrev.milky

import kotlinx.coroutines.flow.MutableSharedFlow
import org.ntqqrev.saltify.ContextFactory
import org.ntqqrev.saltify.Environment
import org.ntqqrev.saltify.event.Event

object MilkyContextFactory : ContextFactory<MilkyInit> {
    override suspend fun createContext(
        init: MilkyInit,
        env: Environment,
        flow: MutableSharedFlow<Event>
    ): MilkyContext = MilkyContext(init, env, flow)
}