package org.ntqqrev.saltify.internal.util

import org.ntqqrev.saltify.dsl.SaltifyPlugin

/**
 * 包装类用于避免星投影的影响，见 [org.ntqqrev.saltify.dsl.config.ApplicationConfig.install]
 */
internal class InstalledPlugin<T : Any>(
    val plugin: SaltifyPlugin<T>,
    val configure: T.() -> Unit
)
