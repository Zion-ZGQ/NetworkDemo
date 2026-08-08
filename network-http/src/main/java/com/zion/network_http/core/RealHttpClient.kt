package com.zion.network_http.core

import com.zion.network_core.api.NetworkKit
import com.zion.network_core.api.NetworkResult
import com.zion.network_http.api.HttpClient
import com.zion.network_http.api.HttpRequest
import com.zion.network_http.api.HttpResponse
import com.zion.network_http.config.HttpConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.concurrent.ConcurrentHashMap

/**
 * HTTP 客户端实现
 *
 * 实现了 HttpClient 接口，提供完整的 HTTP 客户端功能
 *
 * 使用示例：
 * ```kotlin
 * val client = RealHttpClient.getInstance(config)
 * val response = client.execute(request)
 * ```
 */
class RealHttpClient private constructor(
    private val config: HttpConfig
) : HttpClient {

    companion object {
        private const val TAG = "RealHttpClient"

        /**
         * 客户端实例缓存
         */
        private val clientCache = ConcurrentHashMap<String, RealHttpClient>()

        /**
         * 默认客户端实例
         */
        @Volatile
        private var defaultInstance: RealHttpClient? = null

        /**
         * 获取默认客户端实例
         *
         * @return RealHttpClient 实例
         */
        fun getInstance(): RealHttpClient {
            return defaultInstance ?: synchronized(this) {
                defaultInstance ?: RealHttpClient(HttpConfig.DEFAULT).also {
                    defaultInstance = it
                }
            }
        }

        /**
         * 获取指定配置的客户端实例
         *
         * @param config HTTP 配置
         * @return RealHttpClient 实例
         */
        fun getInstance(config: HttpConfig): RealHttpClient {
            if (config == HttpConfig.DEFAULT) {
                return getInstance()
            }

            val cacheKey = generateCacheKey(config)
            return clientCache.getOrPut(cacheKey) {
                RealHttpClient(config)
            }
        }

        /**
         * 清除所有缓存的客户端实例
         */
        fun clearCache() {
            clientCache.values.forEach { it.close() }
            clientCache.clear()
            defaultInstance?.close()
            defaultInstance = null
            NetworkKit.logger.i(TAG, "Client cache cleared")
        }

        /**
         * 生成缓存键
         */
        private fun generateCacheKey(config: HttpConfig): String {
            return "${config.connectTimeout}_${config.readTimeout}_${config.writeTimeout}_${config.enableCache}"
        }
    }

    /**
     * OkHttp 客户端
     */
    private val okHttpClient = OkHttpProvider.getOrCreateClient(config)

    /**
     * 请求执行器
     */
    private val requestExecutor = RequestExecutor(okHttpClient, config.converter)

    /**
     * 缓存的 OkHttp Call 实例（用于取消请求）
     */
    private val callCache = ConcurrentHashMap<String, okhttp3.Call>()

    /**
     * 同步执行请求
     *
     * @param request HTTP 请求
     * @param type 响应类型
     * @return NetworkResult<HttpResponse<T>>
     */
    override fun <T> execute(request: HttpRequest, type: Class<T>): NetworkResult<HttpResponse<T>> {
        NetworkKit.checkInitialized()

        try {
            NetworkKit.logger.d(TAG, "Executing request: ${request.url}")

            // 执行请求
            val result: NetworkResult<HttpResponse<T>> = requestExecutor.execute(request, type)

            // 记录结果
            when (result) {
                is NetworkResult.Success -> {
                    NetworkKit.logger.d(TAG, "Request succeeded: ${request.url}")
                }
                is NetworkResult.Error -> {
                    NetworkKit.logger.e(TAG, "Request failed: ${request.url}", result.exception)
                }
                is NetworkResult.Loading -> {
                    // 不应该出现在这里
                }
            }

            return result
        } catch (e: Exception) {
            NetworkKit.logger.e(TAG, "Request execution failed: ${request.url}", e)
            return NetworkResult.Error(e)
        }
    }

    /**
     * 异步执行请求（挂起函数）
     *
     * @param request HTTP 请求
     * @param type 响应类型
     * @return NetworkResult<HttpResponse<T>>
     */
    override suspend fun <T> executeSuspend(request: HttpRequest, type: Class<T>): NetworkResult<HttpResponse<T>> {
        NetworkKit.checkInitialized()

        return requestExecutor.executeSuspend(request, type)
    }

    /**
     * 执行请求并返回 Flow
     *
     * @param request HTTP 请求
     * @param type 响应类型
     * @return Flow<NetworkResult<HttpResponse<T>>>
     */
    override fun <T> executeFlow(request: HttpRequest, type: Class<T>): Flow<NetworkResult<HttpResponse<T>>> {
        return flow {
            // 发送加载状态
            emit(NetworkResult.Loading())

            // 执行请求
            val result = requestExecutor.executeSuspend<T>(request, type)
            emit(result)
        }.flowOn(NetworkKit.dispatcherProvider.io)
    }

    /**
     * 关闭客户端
     */
    override fun close() {
        try {
            // 取消所有请求
            cancelAll()

            // 关闭 OkHttp 客户端
            okHttpClient.dispatcher.executorService.shutdown()
            okHttpClient.connectionPool.evictAll()

            NetworkKit.logger.i(TAG, "HttpClient closed")
        } catch (e: Exception) {
            NetworkKit.logger.e(TAG, "Failed to close HttpClient", e)
        }
    }

    /**
     * 获取客户端配置
     *
     * @return HTTP 配置
     */
    override fun getConfig(): HttpConfig {
        return config
    }

    /**
     * 取消所有请求
     */
    override fun cancelAll() {
        try {
            // 取消所有缓存的 Call
            callCache.values.forEach { call ->
                if (!call.isCanceled()) {
                    call.cancel()
                }
            }
            callCache.clear()

            // 取消 OkHttp 客户端的所有请求
            okHttpClient.dispatcher.cancelAll()

            NetworkKit.logger.i(TAG, "All requests cancelled")
        } catch (e: Exception) {
            NetworkKit.logger.e(TAG, "Failed to cancel all requests", e)
        }
    }

    /**
     * 取消指定标签的请求
     *
     * @param tag 请求标签
     */
    override fun cancelByTag(tag: String) {
        try {
            // 查找并取消指定标签的 Call
            val callsToRemove = callCache.filter { (key, call) ->
                key.startsWith(tag) && !call.isCanceled()
            }

            callsToRemove.forEach { (key, call) ->
                call.cancel()
                callCache.remove(key)
            }

            NetworkKit.logger.i(TAG, "Cancelled requests with tag: $tag")
        } catch (e: Exception) {
            NetworkKit.logger.e(TAG, "Failed to cancel requests by tag: $tag", e)
        }
    }
}