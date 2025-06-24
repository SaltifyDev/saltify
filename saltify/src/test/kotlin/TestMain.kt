import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.delay
import org.ntqqrev.saltify.SaltifyApp
import org.ntqqrev.saltify.SaltifyAppConfig
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.exists

suspend fun main() {
    println(SaltifyApp.banner)
    println("Welcome to ${SaltifyApp.name} v${SaltifyApp.version}")

    val rootDataPath = Path("data").also {
        if (!it.exists()) {
            it.createDirectories()
            println("Created data directory at $it")
        }
    }
    val rootConfigPath = rootDataPath / "config.json"
    val objectMapper = jacksonObjectMapper()
    val config = if (rootConfigPath.exists()) {
        objectMapper.readValue<SaltifyAppConfig>(rootConfigPath.toFile())
    } else {
        SaltifyAppConfig().also {
            objectMapper.writeValue(rootConfigPath.toFile(), it)
        }
    }

    val app = SaltifyApp(rootDataPath, objectMapper, config)
    app.start()
    delay(Long.MAX_VALUE)
}