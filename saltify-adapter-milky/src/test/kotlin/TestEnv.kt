import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.ktorm.database.Database
import org.ntqqrev.saltify.Context
import org.ntqqrev.saltify.Environment
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.system.measureTimeMillis

val logger = KotlinLogging.logger {}

val testEnv = object : Environment {
    override val scope = CoroutineScope(Dispatchers.IO + CoroutineName("saltify-milky-test"))
    override val rootDataPath: Path = Path("data")
    override val database: Database
        get() = error("Database should not be used in this test.")
}

data class NamedTest(
    val name: String,
    val block: suspend (Context) -> Unit
)

suspend fun runTest(test: NamedTest, ctx: Context) {
    logger.info { "🔍 Running [${test.name}]" }
    val time = measureTimeMillis {
        try {
            test.block(ctx)
            logger.info { "✅ Passed [${test.name}]" }
        } catch (e: Exception) {
            logger.error(e) { "❌ Failed [${test.name}]" }
        }
    }
    logger.info { "⏱️ Duration: ${time}ms\n" }
}