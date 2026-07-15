package org.ntqqrev.saltify.util.logger

import org.ntqqrev.saltify.util.logger.internal.SaltifyApplicationLoggerWriterRedirectToKtorLogger

public fun interface ILogger {
    public fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?)
}

public interface ILoggerFilterable : ILogger {
    public val level: LogLevel
}

public interface ILoggerTaggable : ILogger {
    public val tag: String

    public fun log(level: LogLevel, message: String, throwable: Throwable?): Unit = log(level, tag, message, throwable)
}

public fun interface ILoggerWriter : ILogger

public object SaltifyApplicationLogger : ILogger {
    private val writers = mutableSetOf<ILoggerWriter>()
    public val DefaultLoggerWriter: ILoggerWriter = SaltifyApplicationLoggerWriterRedirectToKtorLogger("Saltify/main")
    init {
        register(DefaultLoggerWriter)
    }
    public fun register(writer: ILoggerWriter): Unit = check(writers.add(writer)) {
        "can't register twice logger-writer:[$writer]"
    }

    public fun unregister(writer: ILoggerWriter): Unit = check(writers.remove(writer)) {
        "can't unregister logger-writer:[$writer] because logger-writer not registered"
    }
    override fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        for (writer in writers) {
            writer.log(level, tag, message, throwable)
        }
    }
}

public enum class LogLevel {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
    NONE
}
