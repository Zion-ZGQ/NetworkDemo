package com.zion.network_core.core.common

/**
 * 扩展函数集合
 */

/**
 * 安全执行，捕获异常
 */
inline fun <T> safeRun(defaultValue: T, block: () -> T): T {
    return try {
        block()
    } catch (e: Exception) {
        defaultValue
    }
}

/**
 * 安全执行，捕获异常（返回 null）
 */
inline fun <T> safeRunOrNull(block: () -> T?): T? {
    return try {
        block()
    } catch (e: Exception) {
        null
    }
}

/**
 * 执行并打印异常（不抛出）
 */
inline fun runCatchingPrint(block: () -> Unit) {
    try {
        block()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}