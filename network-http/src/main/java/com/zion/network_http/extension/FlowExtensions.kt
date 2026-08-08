package com.zion.network_http.extension

import com.zion.network_core.api.NetworkKit
import com.zion.network_core.api.NetworkResult
import com.zion.network_http.api.HttpClient
import com.zion.network_http.api.HttpRequest
import com.zion.network_http.api.HttpResponse
import com.zion.network_http.core.RealHttpClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.io.IOException
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds

/**
 * Flow 扩展函数
 *
 * 提供了便捷的 Flow 操作，用于处理 HTTP 请求
 *
 * 使用示例：
 * ```kotlin
 * requestFlow<User> {
 *     HttpRequest.get("https://api.example.com/users").build()
 * }.collect { result ->
 *     when (result) {
 *         is NetworkResult.Success -> { /* 处理成功 */ }
 *         is NetworkResult.Error -> { /* 处理错误 */ }
 *         is NetworkResult.Loading -> { /* 显示加载 */ }
 *     }
 * }
 * ```
 */

/**
 * 内联扩展函数：执行请求（支持泛型类型推断）
 *
 * @param request HTTP 请求
 * @return NetworkResult<HttpResponse<T>>
 */
inline fun <reified T> HttpClient.execute(request: HttpRequest): NetworkResult<HttpResponse<T>> {
    return execute(request, T::class.java)
}

/**
 * 内联扩展函数：执行请求并返回 Flow（支持泛型类型推断）
 *
 * @param request HTTP 请求
 * @return Flow<NetworkResult<HttpResponse<T>>>
 */
inline fun <reified T> HttpClient.executeFlow(request: HttpRequest): Flow<NetworkResult<HttpResponse<T>>> {
    return executeFlow(request, T::class.java)
}

/**
 * 创建请求 Flow（内联函数，支持泛型类型推断）
 *
 * @param block 请求构建函数
 * @return Flow<NetworkResult<HttpResponse<T>>>
 */
inline fun <reified T> requestFlow(
    block: () -> HttpRequest
): Flow<NetworkResult<HttpResponse<T>>> {
    val client = RealHttpClient.getInstance()
    val request = block()
    return client.executeFlow(request, T::class.java)
}

/**
 * 带重试延迟的 Flow 扩展
 *
 * @param retries 重试次数
 * @param delayMillis 重试延迟时间（毫秒）
 * @return Flow<T>
 */
fun <T> Flow<T>.retryWithDelay(
    retries: Int = 3,
    delayMillis: Long = 1000L
): Flow<T> = retryWhen { cause, attempt ->
    if (cause is IOException && attempt < retries) {
        NetworkKit.logger.w(
            "FlowRetry",
            "Request failed, retrying... (attempt ${attempt + 1}/$retries)"
        )
        delay(delayMillis)
        true
    } else {
        false
    }
}

/**
 * 带指数退避的重试
 *
 * @param maxRetries 最大重试次数
 * @param initialDelayMillis 初始延迟时间（毫秒）
 * @param maxDelayMillis 最大延迟时间（毫秒）
 * @param factor 延迟增长因子
 * @return Flow<T>
 */
fun <T> Flow<T>.retryWithExponentialBackoff(
    maxRetries: Int = 3,
    initialDelayMillis: Long = 1000L,
    maxDelayMillis: Long = 30000L,
    factor: Double = 2.0
): Flow<T> = retryWhen { cause, attempt ->
    if (cause is IOException && attempt < maxRetries) {
        val delayMillis = (initialDelayMillis * factor.pow(attempt.toInt())).toLong()
            .coerceAtMost(maxDelayMillis)

        NetworkKit.logger.w(
            "FlowRetry",
            "Request failed, retrying in ${delayMillis}ms... (attempt ${attempt + 1}/$maxRetries)"
        )
        delay(delayMillis)
        true
    } else {
        false
    }
}

/**
 * 处理网络结果的 Flow 扩展
 *
 * @param onSuccess 成功回调
 * @param onError 错误回调
 * @param onLoading 加载回调
 * @return Flow<NetworkResult<T>>
 */
fun <T> Flow<NetworkResult<T>>.handleResult(
    onSuccess: (T) -> Unit = {},
    onError: (Throwable) -> Unit = {},
    onLoading: (Int) -> Unit = {}
): Flow<NetworkResult<T>> = onEach { result ->
    when (result) {
        is NetworkResult.Success -> onSuccess(result.data)
        is NetworkResult.Error -> onError(result.exception)
        is NetworkResult.Loading -> onLoading(result.progress)
    }
}

/**
 * 过滤成功的 Flow 扩展
 *
 * @return Flow<T> 只包含成功的数据
 */
fun <T> Flow<NetworkResult<T>>.filterSuccess(): Flow<T> = mapNotNull {
    if (it is NetworkResult.Success) {
        it.data
    } else {
        null
    }
}

/**
 * 过滤错误的 Flow 扩展
 *
 * @return Flow<Throwable> 只包含错误
 */
fun <T> Flow<NetworkResult<T>>.filterError(): Flow<Throwable> = mapNotNull {
    if (it is NetworkResult.Error) {
        it.exception
    } else {
        null
    }
}

/**
 * 过滤加载状态的 Flow 扩展
 *
 * @return Flow<Int> 只包含加载进度
 */
fun <T> Flow<NetworkResult<T>>.filterLoading(): Flow<Int> = mapNotNull {
    if (it is NetworkResult.Loading) {
        it.progress
    } else {
        null
    }
}

/**
 * 转换成功数据的 Flow 扩展
 *
 * @param transform 转换函数
 * @return Flow<NetworkResult<R>>
 */
fun <T, R> Flow<NetworkResult<T>>.mapSuccess(
    transform: (T) -> R
): Flow<NetworkResult<R>> = map { result ->
    result.map(transform)
}

/**
 * 捕获异常并转换为 NetworkResult.Error
 *
 * @return Flow<NetworkResult<T>>
 */
fun <T> Flow<T>.catchToNetworkResult(): Flow<NetworkResult<T>> = 
    map<T, NetworkResult<T>> { NetworkResult.Success(it) }
        .catch { e ->
            @Suppress("UNCHECKED_CAST")
            emit(NetworkResult.Error<Nothing>(e) as NetworkResult<T>)
        }

/**
 * 超时扩展
 *
 * @param timeoutMillis 超时时间（毫秒）
 * @return Flow<T>
 */
fun <T> Flow<T>.withTimeout(timeoutMillis: Long): Flow<T> = 
    flow {
        kotlinx.coroutines.withTimeout(timeoutMillis.milliseconds) {
            collect { emit(it) }
        }
    }

/**
 * 节流扩展（在指定时间内只发送第一个事件）
 *
 * @param timeoutMillis 节流时间（毫秒）
 * @return Flow<T>
 */
fun <T> Flow<T>.throttleFirst(timeoutMillis: Long): Flow<T> = flow {
    var lastEmitTime = 0L
    collect { value ->
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastEmitTime >= timeoutMillis) {
            lastEmitTime = currentTime
            emit(value)
        }
    }
}

/**
 * 去重扩展（过滤连续重复的事件）
 *
 * @return Flow<T>
 */
fun <T> Flow<T>.distinctUntilChanged(): Flow<T> = distinctUntilChangedBy { it }

/**
 * 根据键去重扩展
 *
 * @param keySelector 键选择器
 * @return Flow<T>
 */
fun <T, K> Flow<T>.distinctUntilChangedBy(keySelector: (T) -> K): Flow<T> = flow {
    var lastKey: K? = null
    collect { value ->
        val key = keySelector(value)
        if (key != lastKey) {
            lastKey = key
            emit(value)
        }
    }
}

/**
 * 分页加载扩展
 *
 * @param pageSize 每页大小
 * @param loader 加载函数
 * @return Flow<List<T>>
 */
fun <T> Flow<Int>.paged(
    pageSize: Int = 20,
    loader: suspend (page: Int, pageSize: Int) -> List<T>
): Flow<List<T>> = map { page ->
    loader(page, pageSize)
}

/**
 * 最多重试指定次数
 *
 * @param times 重试次数
 * @param predicate 重试条件
 * @return Flow<T>
 */
fun <T> Flow<T>.retryPredicate(
    times: Long = 3,
    predicate: (Throwable) -> Boolean = { true }
): Flow<T> = retry(times) { cause ->
    predicate(cause)
}

/**
 * 累积结果扩展（收集所有结果并返回列表）
 *
 * @return Flow<List<T>>
 */
fun <T> Flow<T>.accumulate(): Flow<List<T>> = flow {
    val items = mutableListOf<T>()
    collect { item ->
        items.add(item)
        emit(items.toList())
    }
}

/**
 * 防抖扩展（在指定时间内只发送最后一个事件）
 *
 * @param timeoutMillis 防抖时间（毫秒）
 * @return Flow<T>
 */
fun <T> Flow<T>.debounceFirst(timeoutMillis: Long): Flow<T> = debounce(timeoutMillis)