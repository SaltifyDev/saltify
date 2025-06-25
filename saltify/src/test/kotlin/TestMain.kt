import kotlinx.coroutines.delay
import org.ntqqrev.saltify.SaltifyApp
import org.ntqqrev.saltify.SaltifyAppContainer
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

suspend fun main() {
    println("${SaltifyApp.name} v${SaltifyApp.version} test run")

    SaltifyAppContainer.startSaltify(Path("data").also {
        if (!it.exists()) {
            it.createDirectories()
            println("Created data directory at $it")
        }
    })
    delay(Long.MAX_VALUE)
}