package org.ntqqrev.saltify

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import org.ntqqrev.saltify.dsl.PluginSpec
import org.ntqqrev.saltify.plugin.PluginMeta
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.streams.asSequence

class SaltifyApp(
    val rootDataPath: Path,
    val config: SaltifyAppConfig
) {
    companion object {
        val banner = """
        |   _____         __ __   _  ____      
        |  / ___/ ____ _ / // /_ (_)/ __/__  __
        |  \__ \ / __ `// // __// // /_ / / / /
        | ___/ // /_/ // // /_ / // __// /_/ / 
        |/____/ \__,_//_/ \__//_//_/   \__, /  
        |                             /____/   
        """.trim().trimMargin()
        const val name = "Saltify"
        const val version = "0.1.0"
    }

    val logger = KotlinLogging.logger { }

    val defaultObjectMapper = jacksonObjectMapper()

    val pluginsPath = rootDataPath / "plugins"
    val pluginSpecs = mutableMapOf<String, PluginSpec<*>>()
    val pluginMetas = mutableMapOf<PluginSpec<*>, PluginMeta>()

    val PluginSpec<*>.meta get() = pluginMetas[this] ?: throw IllegalStateException()

    fun initPluginSpecs() {
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

        metas.forEach { meta ->
            try {
                val pluginClass = loader.loadClass(meta.mainClass)
                val pluginSpec = pluginClass
                    .getDeclaredField("plugin")
                    .get(null) as PluginSpec<*>
                logger.info { "Loaded plugin class: ${meta.id} (${meta.version}) as ${meta.mainClass}" }
                pluginSpecs[meta.id] = pluginSpec
                pluginMetas[pluginSpec] = meta
            } catch (e: Exception) {
                logger.error(e) { "Error loading plugin ${meta.id}: ${e.message}" }
            }
        }
    }
}