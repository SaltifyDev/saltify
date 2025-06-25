package org.ntqqrev.saltify

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.delay
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.exists

object SaltifyAppContainer {
    var current: SaltifyApp? = null

    suspend fun startSaltify(rootDataPath: Path) {
        if (current != null) {
            throw IllegalStateException("Saltify already started")
        }
        val rootConfigPath = rootDataPath / "config.json"
        val objectMapper = jacksonObjectMapper()
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT)
        val config = if (rootConfigPath.exists()) {
            objectMapper.readValue<SaltifyAppConfig>(rootConfigPath.toFile())
        } else {
            SaltifyAppConfig().also {
                objectMapper.writeValue(rootConfigPath.toFile(), it)
            }
        }

        val app = SaltifyApp(rootDataPath, objectMapper, config)
        app.start()
    }
}