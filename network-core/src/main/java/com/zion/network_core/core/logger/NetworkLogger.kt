package com.zion.network_core.core.logger

import android.util.Log
import com.zion.network_core.core.logger.LogLevel.*

/**
 * 网络库默认日志实现
 *
 * 使用 Android Logcat 输出日志
 */
class NetworkLogger : Logger {

    override var level: LogLevel = DEBUG

    private val defaultTag = "NetworkKit"

    override fun v(tag: String, message: String, throwable: Throwable?) {
        if (VERBOSE.shouldLog(level)) {
            Log.v(formatTag(tag), message, throwable)
        }
    }

    override fun d(tag: String, message: String, throwable: Throwable?) {
        if (DEBUG.shouldLog(level)) {
            Log.d(formatTag(tag), message, throwable)
        }
    }

    override fun i(tag: String, message: String, throwable: Throwable?) {
        if (INFO.shouldLog(level)) {
            Log.i(formatTag(tag), message, throwable)
        }
    }

    override fun w(tag: String, message: String, throwable: Throwable?) {
        if (WARN.shouldLog(level)) {
            Log.w(formatTag(tag), message, throwable)
        }
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        if (ERROR.shouldLog(level)) {
            Log.e(formatTag(tag), message, throwable)
        }
    }

    /**
     * 格式化 Tag
     */
    private fun formatTag(tag: String): String {
        return if (tag.isBlank()) defaultTag else "$defaultTag-$tag"
    }
}