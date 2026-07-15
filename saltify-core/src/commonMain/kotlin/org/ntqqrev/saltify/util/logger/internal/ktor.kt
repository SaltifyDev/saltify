package org.ntqqrev.saltify.util.logger.internal

import io.ktor.util.logging.*
import org.ntqqrev.saltify.util.logger.ILoggerWriter
import org.ntqqrev.saltify.util.logger.LogLevel
import org.ntqqrev.saltify.util.logger.LogLevel.*

/**
 * ================================================
 * Author:     iveou
 * Created on: 2026/7/15 11:20
 * ================================================
 */


internal fun SaltifyApplicationLoggerWriterRedirectToKtorLogger(name: String): ILoggerWriter = object : ILoggerWriter {
    private val delegate = KtorSimpleLogger(name)
    override fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        val message = "($tag): $message"
        when (level) {
            TRACE -> if (throwable == null) delegate.trace(message) else delegate.trace(message, throwable)
            DEBUG -> if (throwable == null) delegate.debug(message) else delegate.debug(message, throwable)
            INFO -> if (throwable == null) delegate.info(message) else delegate.info(message, throwable)
            WARN -> if (throwable == null) delegate.warn(message) else delegate.warn(message, throwable)
            ERROR -> if (throwable == null) delegate.error(message) else delegate.error(message, throwable)
            NONE -> Unit
        }
    }
}
