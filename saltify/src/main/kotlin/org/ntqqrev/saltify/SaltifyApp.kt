package org.ntqqrev.saltify

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.ntqqrev.saltify.dsl.PluginSpec
import org.ntqqrev.saltify.event.Event
import org.ntqqrev.saltify.logic.CommandManager
import org.ntqqrev.saltify.logic.ConfigManager
import org.ntqqrev.saltify.plugin.Plugin
import org.ntqqrev.saltify.plugin.PluginMeta
import java.net.URLClassLoader
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries

class SaltifyApp(
    val rootDataPath: Path,
    val defaultObjectMapper: ObjectMapper,
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

    val commandManager = CommandManager(this)
    val configManager = ConfigManager(this)

    val pluginsPath = (rootDataPath / "plugins").also {
        if (!it.exists()) {
            it.createDirectories()
            logger.info { "Created plugins directory at $it" }
        }
    }
    val configPath = (rootDataPath / "config").also {
        if (!it.exists()) {
            it.createDirectories()
            logger.info { "Created config directory at $it" }
        }
    }

    val pluginSpecs = mutableMapOf<String, PluginSpec<*>>()
    val pluginMetas = mutableMapOf<PluginSpec<*>, PluginMeta>()

    val loadedPlugins = mutableMapOf<String, Plugin<*>>()

    val flow = MutableSharedFlow<Event>(
        extraBufferCapacity = config.eventBufferSize,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val PluginSpec<*>.meta get() = pluginMetas[this] ?: throw IllegalStateException()

    fun initPluginSpecs() {
        val loader = URLClassLoader(
            pluginsPath.listDirectoryEntries()
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

    suspend fun loadPlugin(id: String) {
        if (loadedPlugins.containsKey(id)) {
            throw IllegalStateException("Plugin $id is already loaded!")
        }
        val spec = pluginSpecs[id]
        if (spec == null) {
            throw IllegalStateException("Unknown plugin ID: $id")
        }
        val meta = spec.meta
        val pluginConfig = configManager.getConfig(id)
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
                override val rootDataPath: Path = this@SaltifyApp.rootDataPath / "data" / id
            },
            flow
        )
        @Suppress("UNCHECKED_CAST")
        plugin.apply(spec.block as Plugin<Any>.() -> Unit)
        plugin.scope.async {
            plugin.start()
        }.await()
        commandManager.registerAll(plugin)
        loadedPlugins[id] = plugin
    }

    suspend fun start() {
        initPluginSpecs()
        logger.info { "Initialized ${pluginSpecs.size} plugins" }
        val configured = configManager.listAllConfiguredPlugins()
        for (pluginId in configured) {
            try {
                logger.info { "Starting plugin $pluginId" }
                loadPlugin(pluginId)
            } catch (e: Exception) {
                logger.error(e) { "Failed to load plugin $pluginId: ${e.message}" }
            }
        }
        logger.info { "Started ${configured.size} plugins" }
    }
}