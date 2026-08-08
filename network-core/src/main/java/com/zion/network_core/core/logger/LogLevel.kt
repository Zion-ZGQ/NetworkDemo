package com.zion.network_core.core.logger

/**
 * 日志级别枚举
 */
enum class LogLevel(val priority: Int) {
    VERBOSE(2),
    DEBUG(3),
    INFO(4),
    WARN(5),
    ERROR(6),
    NONE(Int.MAX_VALUE);  // 关闭日志

    /**
     * 判断是否应该输出该级别的日志
     */
    fun shouldLog(currentLevel: LogLevel): Boolean {
        return priority >= currentLevel.priority
    }
}