import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import org.ntqqrev.saltify.event.Event
import org.ntqqrev.saltify.event.MessageReceiveEvent
import org.ntqqrev.milky.MilkyContextFactory
import org.ntqqrev.milky.MilkyInit

private val logger = KotlinLogging.logger {}

suspend fun main() {
    val flow = MutableSharedFlow<Event>(extraBufferCapacity = 64)

    val ctx = MilkyContextFactory.createContext(
        init = MilkyInit(
            milkyUrl = "127.0.0.1:3000"
        ),
        env = testEnv,
        flow = flow
    )

    testEnv.scope.launch {
        flow.filterIsInstance<MessageReceiveEvent>()
            .collect {
                logger.info { "receive message: ${it.message.segments}" }
            }
    }

    ctx.start()

    delay(Long.MAX_VALUE)
}
