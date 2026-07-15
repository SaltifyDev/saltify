package org.ntqqrev.saltify.util.logger

/**
 * ================================================
 * Author:     iveou
 * Created on: 2026/7/15 11:31
 * ================================================
 */

public fun ILogger.v(tag: String, message: String, throwable: Throwable? = null): Unit = log(LogLevel.TRACE, tag, message, throwable)
public fun ILogger.d(tag: String, message: String, throwable: Throwable? = null): Unit = log(LogLevel.DEBUG, tag, message, throwable)
public fun ILogger.i(tag: String, message: String, throwable: Throwable? = null): Unit = log(LogLevel.INFO, tag, message, throwable)
public fun ILogger.w(tag: String, message: String, throwable: Throwable? = null): Unit = log(LogLevel.WARN, tag, message, throwable)
public fun ILogger.e(tag: String, message: String, throwable: Throwable? = null): Unit = log(LogLevel.ERROR, tag, message, throwable)
public fun ILogger.verbose(tag: String, message: String, throwable: Throwable? = null): Unit = log(LogLevel.TRACE, tag, message, throwable)
public fun ILogger.debug(tag: String, message: String, throwable: Throwable? = null): Unit = log(LogLevel.DEBUG, tag, message, throwable)
public fun ILogger.info(tag: String, message: String, throwable: Throwable? = null): Unit = log(LogLevel.INFO, tag, message, throwable)
public fun ILogger.warn(tag: String, message: String, throwable: Throwable? = null): Unit = log(LogLevel.WARN, tag, message, throwable)
public fun ILogger.error(tag: String, message: String, throwable: Throwable? = null): Unit = log(LogLevel.ERROR, tag, message, throwable)

public fun ILoggerTaggable.v(message: String, throwable: Throwable? = null): Unit = log(LogLevel.TRACE, message, throwable)
public fun ILoggerTaggable.d(message: String, throwable: Throwable? = null): Unit = log(LogLevel.DEBUG, message, throwable)
public fun ILoggerTaggable.i(message: String, throwable: Throwable? = null): Unit = log(LogLevel.INFO, message, throwable)
public fun ILoggerTaggable.w(message: String, throwable: Throwable? = null): Unit = log(LogLevel.WARN, message, throwable)
public fun ILoggerTaggable.e(message: String, throwable: Throwable? = null): Unit = log(LogLevel.ERROR, message, throwable)
public fun ILoggerTaggable.verbose(message: String, throwable: Throwable? = null): Unit = log(LogLevel.TRACE, message, throwable)
public fun ILoggerTaggable.debug(message: String, throwable: Throwable? = null): Unit = log(LogLevel.DEBUG, message, throwable)
public fun ILoggerTaggable.info(message: String, throwable: Throwable? = null): Unit = log(LogLevel.INFO, message, throwable)
public fun ILoggerTaggable.warn(message: String, throwable: Throwable? = null): Unit = log(LogLevel.WARN, message, throwable)
public fun ILoggerTaggable.error(message: String, throwable: Throwable? = null): Unit = log(LogLevel.ERROR, message, throwable)

public fun ILogger.withTag(name: String): ILoggerTaggable = object : ILoggerTaggable {
    override val tag: String = name

    // 委托的 super 函数
    override fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        this@withTag.log(level, tag, message, throwable)
    }
}

public fun ILogger.withFilter(level: LogLevel): ILoggerFilterable = object : ILoggerFilterable {
    override val level: LogLevel = level

    override fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        if (this.level < level) return
        this@withFilter.log(level, tag, message, throwable)
    }
}
