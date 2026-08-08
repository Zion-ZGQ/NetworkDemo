package com.zion.network_core.core.flow

import com.zion.network_core.api.NetworkKit
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

/**
 * Flow 扩展函数集合
 */

/**
 * 捕获异常并记录日志
 *
 * @param tag 日志标签
 */
fun <T> Flow<T>.catchAndLog(tag: String): Flow<T> = catch { e ->
    NetworkKit.logger.e(tag, "Flow error: ${e.message}", e)
    throw e
}

/**
 * 带延迟的重试
 *
 * @param retries 重试次数
 * @param delay 延迟时间（毫秒）
 */
fun <T> Flow<T>.retryWithDelay(retries: Int, delay: Long): Flow<T> = retryWhen { cause, attempt ->
    if (attempt < retries) {
        NetworkKit.logger.d("Flow", "Retry attempt ${attempt + 1}/$retries after ${delay}ms")
        kotlinx.coroutines.delay(delay)
        true
    } else {
        false
    }
}

/**
 * 指数退避重试
 *
 * @param retries 重试次数
 * @param initialDelay 初始延迟（毫秒）
 * @param maxDelay 最大延迟（毫秒）
 */
fun <T> Flow<T>.retryWithExponentialBackoff(
    retries: Int,
    initialDelay: Long = 1000,
    maxDelay: Long = 30000
): Flow<T> = retryWhen { cause, attempt ->
    if (attempt < retries) {
        val delay = minOf(initialDelay * (1 shl attempt.toInt()), maxDelay)
        NetworkKit.logger.d("Flow", "Retry attempt ${attempt + 1}/$retries with delay ${delay}ms")
        kotlinx.coroutines.delay(delay)
        true
    } else {
        false
    }
}

/**
 * 节流（在指定时间窗口内只发射第一个值）
 *
 * @param windowDuration 时间窗口（毫秒）
 */
@FlowPreview
@ExperimentalTime
fun <T> Flow<T>.throttleFirst(windowDuration: Long): Flow<T> = flow {
    var lastEmitTime = 0L
    collect { value ->
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastEmitTime >= windowDuration) {
            lastEmitTime = currentTime
            emit(value)
        }
    }
}

/**
 * 去抖动（在指定时间内没有新值才发射）
 *
 * @param timeout 超时时间（毫秒）
 */
@FlowPreview
@ExperimentalTime
fun <T> Flow<T>.debounceByTime(timeout: Long): Flow<T> = debounce(timeout)

/**
 * 超时控制
 *
 * @param timeout 超时时间（毫秒）
 */
@ExperimentalTime
fun <T> Flow<T>.timeoutIn(timeout: Long): Flow<T> = timeout(timeout.milliseconds)

/**
 * 去重（根据指定条件）
 *
 * @param predicate 判断条件
 */
fun <T> Flow<T>.distinctUntilChangedBy(predicate: (T) -> Any): Flow<T> = distinctUntilChangedBy(predicate)

/**
 * 只在值改变时发射
 */
fun <T : Any> Flow<T>.filterNotNullValues(): Flow<T> = filterNotNull()

/**
 * 累积发射（批量发送）
 *
 * @param batchSize 批量大小
 * @param timeout 超时时间（毫秒）
 */
fun <T> Flow<T>.batch(batchSize: Int, timeout: Long = 1000): Flow<List<T>> = flow {
    val batch = mutableListOf<T>()
    var lastEmitTime = System.currentTimeMillis()

    collect { value ->
        batch.add(value)
        val currentTime = System.currentTimeMillis()

        if (batch.size >= batchSize || (currentTime - lastEmitTime) >= timeout) {
            emit(batch.toList())
            batch.clear()
            lastEmitTime = currentTime
        }
    }

    // 发射剩余的数据
    if (batch.isNotEmpty()) {
        emit(batch.toList())
    }
}

/**
 * 映射并过滤空值
 */
inline fun <T, R : Any> Flow<T>.mapNotNull(crossinline transform: suspend (T) -> R?): Flow<R> =
    map(transform).filterNotNull()

/**
 * 监听并打印日志（调试用）
 */
fun <T> Flow<T>.debug(tag: String): Flow<T> = onEach { value ->
    NetworkKit.logger.d(tag, "Emitting: $value")
}