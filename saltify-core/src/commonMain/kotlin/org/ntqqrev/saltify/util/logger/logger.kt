package org.ntqqrev.saltify.util.logger

import org.ntqqrev.saltify.util.logger.internal.SaltifyApplicationLoggerWriterRedirectToKtorLogger

/**
 * 核心日志接口。所有日志操作最终都通过此接口输出。
 */
public fun interface ILogger {
    public fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?)
}

/**
 * 可过滤的日志接口。低于 [level] 的日志条目将被静默丢弃。
 */
public interface ILoggerFilterable : ILogger {
    public val level: LogLevel
}

/**
 * 预绑定标签的日志接口。调用 [log] 时无需重复传入 tag。
 */
public interface ILoggerTaggable : ILogger {
    public val tag: String

    public fun log(level: LogLevel, message: String, throwable: Throwable?): Unit = log(level, tag, message, throwable)
}

/**
 * 日志写入器接口。实现此接口可自定义日志的最终输出目标。
 */
public fun interface ILoggerWriter : ILogger

/**
 * 全局应用日志单例。
 *
 * 理论上所有的日志都应该是 [SaltifyApplicationLogger] 的委托。
 *
 * 通过注册 [ILoggerWriter] 将日志分发到多个输出端。
 *
 * 例如：通过 [ILogger.i(tag,message,throwable)] 和 [ILoggerTaggable.i(message,throwable)] 输出日志
 */
public object SaltifyApplicationLogger : ILogger {
    private val writers = mutableSetOf<ILoggerWriter>()
    public val DefaultLoggerWriter: ILoggerWriter = SaltifyApplicationLoggerWriterRedirectToKtorLogger("")
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

/**
 * 日志级别。按严重程度升序排列，[NONE] 不输出任何日志。
 */
public enum class LogLevel {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
    NONE
}
