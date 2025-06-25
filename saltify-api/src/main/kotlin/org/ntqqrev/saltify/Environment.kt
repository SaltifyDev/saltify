package org.ntqqrev.saltify

import kotlinx.coroutines.CoroutineScope
import org.ktorm.database.Database
import java.nio.file.Path

interface Environment {
    /**
     * The parent coroutine context of this environment.
     */
    val scope: CoroutineScope

    /**
     * The path to the root data directory.
     */
    val rootDataPath: Path

    /**
     * The database for all plugins & contexts to use.
     */
    val database: Database
}