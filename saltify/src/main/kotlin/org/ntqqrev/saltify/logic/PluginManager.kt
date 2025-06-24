package org.ntqqrev.saltify.logic

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.ntqqrev.saltify.Environment
import org.ntqqrev.saltify.SaltifyApp
import org.ntqqrev.saltify.dsl.PluginSpec
import org.ntqqrev.saltify.event.Event
import org.ntqqrev.saltify.plugin.Plugin
import org.ntqqrev.saltify.plugin.PluginMeta
import java.net.URLClassLoader
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.listDirectoryEntries

class PluginManager(val app: SaltifyApp) {
    private val logger = KotlinLogging.logger {}

    val pluginSpecs = mutableMapOf<String, PluginSpec<*>>()
    val pluginMetas = mutableMapOf<PluginSpec<*>, PluginMeta>()

    val loadedPlugins = mutableMapOf<String, Plugin<*>>()

    val flow = MutableSharedFlow<Event>(
        extraBufferCapacity = app.config.eventBufferSize,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val PluginSpec<*>.meta get() = pluginMetas[this] ?: throw IllegalStateException()

    fun initPluginSpecs() {
        val loader = URLClassLoader(
            app.pluginsPath.listDirectoryEntries()
                .filter { it.fileName.toString().endsWith(".jar") }
                .map { it.toUri().toURL() }
                .toList().toTypedArray(),
            this::class.java.classLoader
        )
        val metas = loader.getResources("saltify.json")
            .asSequence()
            .map { app.defaultObjectMapper.readValue<PluginMeta>(it) }
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
                    .getDeclaredMethod("getPlugin")
                    .invoke(null) as PluginSpec<*>
                logger.info { "Loaded plugin class: ${meta.id} (${meta.version}) as ${meta.mainClass}" }
                pluginSpecs[meta.id] = pluginSpec
                pluginMetas[pluginSpec] = meta
            } catch (e: Exception) {
                logger.error(e) { "Error loading plugin ${meta.id}: ${e.message}" }
            }
        }
    }

    suspend fun loadPlugin(id: String) {
        if (loadedPlugins.containsKey(id)) {
            throw IllegalStateException("Plugin $id is already loaded!")
        }
        val spec = pluginSpecs[id]
        if (spec == null) {
            throw IllegalStateException("Unknown plugin ID: $id")
        }
        val meta = spec.meta
        val pluginConfig = app.configManager.getConfig(id)
        val plugin = Plugin(
            meta,
            pluginConfig,
            object : Environment {
                override val scope = CoroutineScope(
                    CoroutineName("SaltifyPlugin-$id") +
                            CoroutineExceptionHandler { _, exception ->
                                logger.error(exception) {
                                    "Uncaught exception occurred in plugin $id: ${exception.message}"
                                }
                            }
                )
                override val rootDataPath: Path = app.rootDataPath / "data" / id
            },
            flow
        )
        @Suppress("UNCHECKED_CAST")
        plugin.apply(spec.block as Plugin<Any>.() -> Unit)
        plugin.scope.async {
            plugin.start()
        }.await()
        app.commandManager.registerAll(plugin)
        loadedPlugins[id] = plugin
    }
}