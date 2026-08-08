package com.zion.network_http.core

import com.zion.network_core.api.NetworkKit
import com.zion.network_core.api.NetworkResult
import com.zion.network_http.api.HttpRequest
import com.zion.network_http.api.HttpResponse
import com.zion.network_http.converter.Converter
import com.zion.network_http.converter.fromBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

/**
 * 请求执行器
 *
 * 负责实际的 HTTP 请求执行，包括请求构建、执行和响应处理
 *
 * 使用示例：
 * ```kotlin
 * val executor = RequestExecutor(okHttpClient, converter)
 * val result = executor.execute(request)
 * ```
 */
class RequestExecutor(
    private val client: OkHttpClient,
    private val converter: Converter
) {

    companion object {
        private const val TAG = "RequestExecutor"
    }

    /**
     * 执行请求（同步）
     *
     * @param request HTTP 请求
     * @param type 响应类型（用于反序列化）
     * @return NetworkResult<HttpResponse<T>>
     */
    fun <T> execute(request: HttpRequest, type: Class<T>): NetworkResult<HttpResponse<T>> {
        return try {
            // 构建 OkHttp 请求
            val okHttpRequest = buildOkHttpRequest(request)

            // 执行请求
            val response = client.newCall(okHttpRequest).execute()

            // 处理响应
            handleResponse(response, type)
        } catch (e: Exception) {
            NetworkKit.logger.e(TAG, "Request failed: ${request.url}", e)
            NetworkResult.Error(e)
        }
    }


    /**
     * 执行请求（异步，挂起函数）
     *
     * @param request HTTP 请求
     * @param type 响应类型（用于反序列化）
     * @return NetworkResult<HttpResponse<T>>
     */
    suspend fun <T> executeSuspend(request: HttpRequest, type: Class<T>): NetworkResult<HttpResponse<T>> {
        return withContext(NetworkKit.dispatcherProvider.io) {
            execute(request, type)
        }
    }

    /**
     * 构建 OkHttp 请求
     *
     *
     * @param request HTTP 请求
     * @return OkHttp Request
     */
    private fun buildOkHttpRequest(request: HttpRequest): Request {
        val builder = Request.Builder()

        // 设置 URL
        var url = request.url

        // 如果有参数且是 GET 请求，添加参数到 URL
        if (request.params.isNotEmpty() && request.method == HttpRequest.HttpMethod.GET) {
            val separator = if (url.contains("?")) "&" else "?"
            val params = request.params.entries.joinToString("&") { "${it.key}=${it.value}" }
            url = "$url$separator$params"
        }

        builder.url(url)

        // 设置请求方法
        when (request.method) {
            HttpRequest.HttpMethod.GET -> builder.get()
            HttpRequest.HttpMethod.POST -> builder.post(request.body ?: "".toRequestBody())
            HttpRequest.HttpMethod.PUT -> builder.put(request.body ?: "".toRequestBody())
            HttpRequest.HttpMethod.DELETE -> builder.delete(request.body)
            HttpRequest.HttpMethod.PATCH -> builder.patch(request.body ?: "".toRequestBody())
            HttpRequest.HttpMethod.HEAD -> builder.head()
            HttpRequest.HttpMethod.OPTIONS -> builder.method("OPTIONS", request.body)
        }

        // 设置请求头
        request.headers.forEach { (name, value) ->
            builder.addHeader(name, value)
        }

        // 设置标签（用于取消请求）
        if (request.tag != null) {
            builder.tag(request.tag)
        }

        return builder.build()
    }

    /**
     * 处理响应
     *
     * @param response OkHttp Response
     * @param type 响应类型
     * @return NetworkResult<HttpResponse<T>>
     */
    private fun <T> handleResponse(response: okhttp3.Response, type: Class<T>): NetworkResult<HttpResponse<T>> {
        return try {
            // 检查响应是否成功
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Unknown error"
                val exception = IOException("HTTP ${response.code}: $errorBody")
                return NetworkResult.Error(exception)
            }

            // 转换响应体
            val body: T? = if (response.body != null) {
                converter.fromBody(response.body!!, type)
            } else {
                null
            }

            // 构建响应对象
            val httpResponse = HttpResponse.from(response, body)

            NetworkKit.logger.d(TAG, "Request succeeded: ${response.request.url}")

            NetworkResult.Success(httpResponse)
        } catch (e: Exception) {
            NetworkKit.logger.e(TAG, "Response handling failed", e)
            NetworkResult.Error(e)
        } finally {
            response.close()
        }
    }
}