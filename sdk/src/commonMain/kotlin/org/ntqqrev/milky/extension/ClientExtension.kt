package org.ntqqrev.milky.extension

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.ntqqrev.milky.Event
import org.ntqqrev.milky.IncomingSegment
import org.ntqqrev.milky.core.MilkyClient

public inline fun <reified T : Event> MilkyClient.on(
    crossinline block: suspend MilkyClient.(event: T) -> Unit
): Job = clientScope.launch {
    eventFlow.filter { it is T }.collect { block(it as T) }
}

public inline fun MilkyClient.command(
    name: String,
    prefix: String = "/",
    crossinline block: suspend MilkyClient.(event: Event.MessageReceive) -> Unit
): Job = on<Event.MessageReceive> { event ->
    val firstSegment = event.data.segments.firstOrNull() as? IncomingSegment.Text ?: return@on

    if (firstSegment.data.text.startsWith("$prefix$name")) {
        block(event)
    }
}
