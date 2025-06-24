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
import org.ntqqrev.saltify.logic.PluginManager
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
    val pluginManager = PluginManager(this)

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

    suspend fun start() {
        pluginManager.initPluginSpecs()
        logger.info { "Initialized ${pluginManager.pluginSpecs.size} plugins" }
        val configured = configManager.listAllConfiguredPlugins()
        for (pluginId in configured) {
            try {
                logger.info { "Starting plugin $pluginId" }
                pluginManager.loadPlugin(pluginId)
            } catch (e: Exception) {
                logger.error(e) { "Failed to load plugin $pluginId: ${e.message}" }
            }
        }
        logger.info { "Started ${configured.size} plugins" }
    }
}