import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.ktorm.database.Database
import org.ntqqrev.saltify.Environment
import java.nio.file.Path
import kotlin.io.path.Path

val testEnv = object : Environment {
    override val scope = CoroutineScope(Dispatchers.IO + CoroutineName("saltify-milky-test"))
    override val rootDataPath: Path = Path("data")
    override val database: Database
        get() = error("Database should not be used in this test.")
}