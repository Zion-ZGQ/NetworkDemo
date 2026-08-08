package com.zion.network_core.api

import com.zion.network_core.core.executor.DispatcherProvider
import com.zion.network_core.core.logger.LogLevel
import com.zion.network_core.core.logger.Logger

/**
 * 网络库全局配置
 *
 * @param logLevel 日志级别
 * @param logger 日志实现
 * @param dispatcherProvider 协程调度器提供者
 * @param connectTimeout 连接超时时间（毫秒）
 * @param readTimeout 读取超时时间（毫秒）
 * @param writeTimeout 写入超时时间（毫秒）
 * @param debug 是否开启调试模式
 */
data class NetworkConfig(
    val logLevel: LogLevel = LogLevel.DEBUG,
    val logger: Logger? = null,
    val dispatcherProvider: DispatcherProvider? = null,
    val connectTimeout: Long = 30_000L,
    val readTimeout: Long = 30_000L,
    val writeTimeout: Long = 30_000L,
    val debug: Boolean = false
) {

    /**
     * Builder 模式配置类
     */
    class Builder {
        private var logLevel: LogLevel = LogLevel.DEBUG
        private var logger: Logger? = null
        private var dispatcherProvider: DispatcherProvider? = null
        private var connectTimeout: Long = 30_000L
        private var readTimeout: Long = 30_000L
        private var writeTimeout: Long = 30_000L
        private var debug: Boolean = false

        fun setLogLevel(level: LogLevel) = apply {
            this.logLevel = level
        }

        fun setLogger(logger: Logger) = apply {
            this.logger = logger
        }

        fun setDispatcherProvider(provider: DispatcherProvider) = apply {
            this.dispatcherProvider = provider
        }

        fun setConnectTimeout(timeout: Long) = apply {
            this.connectTimeout = timeout
        }

        fun setReadTimeout(timeout: Long) = apply {
            this.readTimeout = timeout
        }

        fun setWriteTimeout(timeout: Long) = apply {
            this.writeTimeout = timeout
        }

        fun setDebug(debug: Boolean) = apply {
            this.debug = debug
        }

        fun build(): NetworkConfig {
            return NetworkConfig(
                logLevel = logLevel,
                logger = logger,
                dispatcherProvider = dispatcherProvider,
                connectTimeout = connectTimeout,
                readTimeout = readTimeout,
                writeTimeout = writeTimeout,
                debug = debug
            )
        }
    }
}