package org.ntqqrev.saltify.entity

import org.ntqqrev.saltify.dsl.SaltifyPlugin
import org.ntqqrev.saltify.dsl.config.SaltifyApplicationConfig

/**
 * 包装类用于避免星投影的影响，见 [SaltifyApplicationConfig.install]
 */
internal class InstalledPlugin<T : Any>(
    val plugin: SaltifyPlugin<T>,
    val configure: T.() -> Unit
)
