package com.zion.network_http

import com.google.gson.Gson
import com.zion.network_core.api.NetworkResult
import com.zion.network_http.api.HttpRequest
import com.zion.network_http.api.HttpResponse
import com.zion.network_http.core.RealHttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * HTTP 全局配置对象
 *
 * 用于配置全局的 HTTP 请求参数，如 baseUrl、默认请求头、超时时间等。
 * 建议在 Application 的 onCreate 中进行配置。
 *
 * 使用示例：
 * ```kotlin
 * HttpConfig.apply {
 *     baseUrl = "https://api.example.com"
 *     defaultHeaders = mapOf(
 *         "Content-Type" to "application/json",
 *         "Accept" to "application/json"
 *     )
 * }
 * ```
 */
object HttpConfig {
    /**
     * 基础 URL
     * 所有相对路径的请求都会自动拼接这个 baseUrl
     */
    var baseUrl: String = ""

    /**
     * 全局默认请求头
     * 所有请求都会自动添加这些请求头
     */
    var defaultHeaders: Map<String, String> = emptyMap()

    /**
     * 连接超时时间（毫秒）
     */
    var connectTimeout: Long = 30_000L

    /**
     * 读取超时时间（毫秒）
     */
    var readTimeout: Long = 30_000L

    /**
     * 写入超时时间（毫秒）
     */
    var writeTimeout: Long = 30_000L
}

/**
 * 发送 GET 请求（同步版本）
 *
 * @param url 请求 URL（相对路径会自动拼接 baseUrl）
 * @param headers 本次请求额外的请求头（可选）
 * @param timeout 本次请求的超时时间（可选，默认使用全局配置）
 * @return 网络请求结果
 */
suspend inline fun <reified T> httpGet(
    url: String,
    headers: Map<String, String> = emptyMap(),
    timeout: Long? = null
): NetworkResult<HttpResponse<T>> {
    return executeRequest<T>(
        url = url,
        method = HttpRequest.HttpMethod.GET,
        headers = headers,
        timeout = timeout
    )
}

/**
 * 发送 GET 请求（Flow 版本）
 *
 * @param url 请求 URL（相对路径会自动拼接 baseUrl）
 * @param headers 本次请求额外的请求头（可选）
 * @param timeout 本次请求的超时时间（可选，默认使用全局配置）
 * @return 网络请求结果流
 */
inline fun <reified T> httpGetFlow(
    url: String,
    headers: Map<String, String> = emptyMap(),
    timeout: Long? = null
): Flow<NetworkResult<HttpResponse<T>>> {
    return executeRequestFlow<T>(
        url = url,
        method = HttpRequest.HttpMethod.GET,
        headers = headers,
        timeout = timeout
    )
}

/**
 * 发送 POST 请求（同步版本）
 *
 * @param url 请求 URL（相对路径会自动拼接 baseUrl）
 * @param body 请求体（会自动序列化为 JSON）
 * @param headers 本次请求额外的请求头（可选）
 * @param timeout 本次请求的超时时间（可选，默认使用全局配置）
 * @return 网络请求结果
 */
suspend inline fun <reified T> httpPost(
    url: String,
    body: Any? = null,
    headers: Map<String, String> = emptyMap(),
    timeout: Long? = null
): NetworkResult<HttpResponse<T>> {
    return executeRequest<T>(
        url = url,
        method = HttpRequest.HttpMethod.POST,
        body = body,
        headers = headers,
        timeout = timeout
    )
}

/**
 * 发送 POST 请求（Flow 版本）
 *
 * @param url 请求 URL（相对路径会自动拼接 baseUrl）
 * @param body 请求体（会自动序列化为 JSON）
 * @param headers 本次请求额外的请求头（可选）
 * @param timeout 本次请求的超时时间（可选，默认使用全局配置）
 * @return 网络请求结果流
 */
inline fun <reified T> httpPostFlow(
    url: String,
    body: Any? = null,
    headers: Map<String, String> = emptyMap(),
    timeout: Long? = null
): Flow<NetworkResult<HttpResponse<T>>> {
    return executeRequestFlow<T>(
        url = url,
        method = HttpRequest.HttpMethod.POST,
        body = body,
        headers = headers,
        timeout = timeout
    )
}

/**
 * 发送 PUT 请求（同步版本）
 */
suspend inline fun <reified T> httpPut(
    url: String,
    body: Any? = null,
    headers: Map<String, String> = emptyMap(),
    timeout: Long? = null
): NetworkResult<HttpResponse<T>> {
    return executeRequest<T>(
        url = url,
        method = HttpRequest.HttpMethod.PUT,
        body = body,
        headers = headers,
        timeout = timeout
    )
}

/**
 * 发送 PUT 请求（Flow 版本）
 */
inline fun <reified T> httpPutFlow(
    url: String,
    body: Any? = null,
    headers: Map<String, String> = emptyMap(),
    timeout: Long? = null
): Flow<NetworkResult<HttpResponse<T>>> {
    return executeRequestFlow<T>(
        url = url,
        method = HttpRequest.HttpMethod.PUT,
        body = body,
        headers = headers,
        timeout = timeout
    )
}

/**
 * 发送 DELETE 请求（同步版本）
 */
suspend inline fun <reified T> httpDelete(
    url: String,
    headers: Map<String, String> = emptyMap(),
    timeout: Long? = null
): NetworkResult<HttpResponse<T>> {
    return executeRequest<T>(
        url = url,
        method = HttpRequest.HttpMethod.DELETE,
        headers = headers,
        timeout = timeout
    )
}

/**
 * 发送 DELETE 请求（Flow 版本）
 */
inline fun <reified T> httpDeleteFlow(
    url: String,
    headers: Map<String, String> = emptyMap(),
    timeout: Long? = null
): Flow<NetworkResult<HttpResponse<T>>> {
    return executeRequestFlow<T>(
        url = url,
        method = HttpRequest.HttpMethod.DELETE,
        headers = headers,
        timeout = timeout
    )
}

/**
 * 执行 HTTP 请求（内部实现）
 */
suspend inline fun <reified T> executeRequest(
    url: String,
    method: HttpRequest.HttpMethod,
    body: Any? = null,
    headers: Map<String, String> = emptyMap(),
    timeout: Long? = null
): NetworkResult<HttpResponse<T>> {
    val request = buildRequest(url, method, body, headers, timeout)
    return RealHttpClient.getInstance().executeSuspend(request, T::class.java)
}

/**
 * 执行 HTTP 请求（Flow 版本，内部实现）
 */
inline fun <reified T> executeRequestFlow(
    url: String,
    method: HttpRequest.HttpMethod,
    body: Any? = null,
    headers: Map<String, String> = emptyMap(),
    timeout: Long? = null
): Flow<NetworkResult<HttpResponse<T>>> {
    val request = buildRequest(url, method, body, headers, timeout)
    return RealHttpClient.getInstance().executeFlow(request, T::class.java)
}
private val gson = Gson()
/**
 * 构建 HttpRequest 对象
 */
fun buildRequest(
    url: String,
    method: HttpRequest.HttpMethod,
    body: Any? = null,
    headers: Map<String, String> = emptyMap(),
    timeout: Long? = null
): HttpRequest {
    // 拼接 baseUrl
    val fullUrl = if (url.startsWith("http://") || url.startsWith("https://")) {
        url
    } else {
        HttpConfig.baseUrl.removeSuffix("/") + "/" + url.removePrefix("/")
    }

    val builder = HttpRequest.Builder()
        .url(fullUrl)
        .apply {
            // 添加全局默认请求头
            HttpConfig.defaultHeaders.forEach { (key, value) ->
                addHeader(key, value)
            }

            // 添加本次请求的请求头
            headers.forEach { (key, value) ->
                addHeader(key, value)
            }

            // 设置超时时间
            timeout?.let {
                timeout(it)
            }
        }

    // 根据 HTTP 方法设置请求
    return when (method) {
        HttpRequest.HttpMethod.GET -> builder.get().build()
        HttpRequest.HttpMethod.POST -> {
            val jsonBody = if (body != null)gson.toJson(body) else "{}"
            builder.postJson(jsonBody).build()
        }
        HttpRequest.HttpMethod.PUT -> builder.put().build()
        HttpRequest.HttpMethod.DELETE -> builder.delete().build()
        HttpRequest.HttpMethod.PATCH -> builder.patch().build()
        HttpRequest.HttpMethod.HEAD -> builder.head().build()
        HttpRequest.HttpMethod.OPTIONS -> builder.get().build() // OPTIONS 暂时用 GET 实现
    }
}