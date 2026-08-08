package com.zion.network_core.core.logger

/**
 * 日志接口
 *
 * 允许用户自定义日志实现
 */
interface Logger {

    /**
     * 日志级别
     */
    var level: LogLevel

    /**
     * 输出 VERBOSE 级别日志
     */
    fun v(tag: String, message: String, throwable: Throwable? = null)

    /**
     * 输出 DEBUG 级别日志
     */
    fun d(tag: String, message: String, throwable: Throwable? = null)

    /**
     * 输出 INFO 级别日志
     */
    fun i(tag: String, message: String, throwable: Throwable? = null)

    /**
     * 输出 WARN 级别日志
     */
    fun w(tag: String, message: String, throwable: Throwable? = null)

    /**
     * 输出 ERROR 级别日志
     */
    fun e(tag: String, message: String, throwable: Throwable? = null)
}