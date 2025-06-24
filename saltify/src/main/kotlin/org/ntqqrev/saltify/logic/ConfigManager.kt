package org.ntqqrev.saltify.logic

import org.ntqqrev.saltify.SaltifyApp
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.reflect.full.primaryConstructor

class ConfigManager(val app: SaltifyApp) {
    private fun Path.ofPlugin(pluginId: String): Path {
        return this / "$pluginId.config.json"
    }

    fun createConfig(pluginId: String): Any {
        if (!app.pluginSpecs.containsKey(pluginId)) {
            throw IllegalArgumentException("Plugin with ID '$pluginId' does not exist.")
        }
        val pluginConfigPath = app.configPath.ofPlugin(pluginId)
        if (pluginConfigPath.exists()) {
            throw IllegalStateException("Config file for plugin '$pluginId' already exists at $pluginConfigPath.")
        }
        val configClass = app.pluginSpecs[pluginId]!!.configClass
        val configDraft = configClass.primaryConstructor!!.callBy(emptyMap())
        app.defaultObjectMapper.writeValue(pluginConfigPath.toFile(), configDraft)
        return configDraft
    }

    private fun ensureConfigPathOf(pluginId: String): Path {
        val pluginConfigPath = app.configPath.ofPlugin(pluginId)
        if (!pluginConfigPath.exists()) {
            throw IllegalStateException("Config file for plugin '$pluginId' does not exist at $pluginConfigPath.")
        }
        return pluginConfigPath
    }

    fun getConfig(pluginId: String): Any {
        if (!app.pluginSpecs.containsKey(pluginId)) {
            throw IllegalArgumentException("Plugin with ID '$pluginId' does not exist.")
        }
        val pluginConfigPath = ensureConfigPathOf(pluginId)
        return app.defaultObjectMapper.readValue(
            pluginConfigPath.toFile(),
            app.pluginSpecs[pluginId]!!.configClass.java
        )
    }

    fun saveConfig(pluginId: String, config: String) {
        if (!app.pluginSpecs.containsKey(pluginId)) {
            throw IllegalArgumentException("Plugin with ID '$pluginId' does not exist.")
        }
        val pluginConfigPath = ensureConfigPathOf(pluginId)
        val configClass = app.pluginSpecs[pluginId]!!.configClass
        val configObject = app.defaultObjectMapper.readValue(config, configClass.java)
        app.defaultObjectMapper.writeValue(pluginConfigPath.toFile(), configObject)
    }

    fun deleteConfig(pluginId: String) {
        if (!app.pluginSpecs.containsKey(pluginId)) {
            throw IllegalArgumentException("Plugin with ID '$pluginId' does not exist.")
        }
        if (app.loadedPlugins.containsKey(pluginId)) {
            throw IllegalStateException("Cannot delete config for plugin '$pluginId' while it is loaded.")
        }
        val pluginConfigPath = app.configPath.ofPlugin(pluginId)
        if (!pluginConfigPath.exists()) {
            throw IllegalStateException("Config file for plugin '$pluginId' does not exist at $pluginConfigPath.")
        }
        pluginConfigPath.toFile().delete()
    }

    fun listAllConfiguredPlugins(): List<String> {
        return app.pluginSpecs.keys.filter { app.configPath.ofPlugin(it).exists() }
    }
}