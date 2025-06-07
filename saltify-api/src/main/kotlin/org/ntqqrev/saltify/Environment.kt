package org.ntqqrev.saltify

import kotlinx.coroutines.CoroutineScope
import java.nio.file.Path
import kotlin.coroutines.CoroutineContext

interface Environment {
    /**
     * The parent coroutine context of this environment.
     */
    val scope: CoroutineScope

    /**
     * The path to the root data directory.
     */
    val rootDataPath: Path
}