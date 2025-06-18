package org.ntqqrev.saltify

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import org.ntqqrev.saltify.plugin.PluginMeta
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.streams.asSequence

class SaltifyApp(
    val rootDataPath: Path,
) {
    val logger = KotlinLogging.logger { }

    val defaultObjectMapper = jacksonObjectMapper()

    val pluginsPath = rootDataPath / "plugins"
    internal val pluginClasses = mutableMapOf<String, Class<*>>()

    internal fun loadPluginClasses() {
        val loader = URLClassLoader(
            Files.list(pluginsPath)
                .asSequence()
                .filter { it.endsWith(".jar") }
                .map { it.toUri().toURL() }
                .toList().toTypedArray(),
            this::class.java.classLoader
        )
        val metas = loader.getResources("saltify.json")
            .asSequence()
            .map { defaultObjectMapper.readValue<PluginMeta>(it) }
            .toList()

        // check for duplicate plugin IDs
        val duplicateIds = metas.groupBy { it.id }
            .filter { it.value.size > 1 }
            .keys
        if (duplicateIds.isNotEmpty()) {
            throw IllegalStateException("Duplicate plugin IDs found: ${duplicateIds.joinToString(", ")}")
        }

        // load plugin classes
        metas.forEach { meta ->
            try {
                val pluginClass = loader.loadClass(meta.mainClass)
                pluginClasses[meta.id] = pluginClass
                logger.info { "Loaded plugin class: ${meta.id} (${meta.version}) as ${meta.mainClass}" }
            } catch (e: Exception) {
                logger.error(e) { "Error loading plugin ${meta.id}: ${e.message}" }
            }
        }
    }
}