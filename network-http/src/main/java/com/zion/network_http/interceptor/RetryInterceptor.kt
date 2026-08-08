package com.zion.network_http.interceptor

import com.zion.network_core.api.NetworkKit
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * 重试拦截器
 *
 * 用于在请求失败时自动重试，支持配置重试次数和重试延迟
 *
 * 使用示例：
 * ```kotlin
 * val interceptor = RetryInterceptor.Builder()
 *     .retryCount(3)
 *     .retryDelay(1000)
 *     .build()
 * ```
 */
class RetryInterceptor private constructor(
    private val retryCount: Int,
    private val retryDelay: Long,
    private val retryOn: (IOException) -> Boolean
) : Interceptor {

    companion object {
        private const val TAG = "RetryInterceptor"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var lastException: IOException? = null

        // 重试循环
        for (retryAttempt in 0..retryCount) {
            try {
                // 执行请求
                val response = chain.proceed(request)

                // 如果响应成功，直接返回
                if (response.isSuccessful) {
                    return response
                }

                // 如果是客户端错误（4xx），不重试
                if (response.code in 400..499) {
                    return response
                }

                // 关闭响应体，准备重试
                response.close()

                // 记录重试日志
                if (retryAttempt < retryCount) {
                    NetworkKit.logger.w(
                        TAG,
                        "Request failed with code ${response.code}, retrying... (attempt ${retryAttempt + 1}/$retryCount)"
                    )

                    // 延迟后重试
                    if (retryDelay > 0) {
                        Thread.sleep(retryDelay)
                    }
                }
            } catch (e: IOException) {
                lastException = e

                // 检查是否应该重试此异常
                if (!retryOn(e)) {
                    throw e
                }

                // 记录重试日志
                if (retryAttempt < retryCount) {
                    NetworkKit.logger.w(
                        TAG,
                        "Request failed: ${e.message}, retrying... (attempt ${retryAttempt + 1}/$retryCount)",
                        e
                    )

                    // 延迟后重试
                    if (retryDelay > 0) {
                        Thread.sleep(retryDelay)
                    }
                }
            }
        }

        // 所有重试都失败，抛出最后一次异常
        throw lastException ?: IOException("Request failed after $retryCount retries")
    }

    /**
     * Builder 类
     */
    class Builder {
        private var retryCount: Int = 3
        private var retryDelay: Long = 1000L
        private var retryOn: (IOException) -> Boolean = { true }

        /**
         * 设置重试次数
         *
         * @param count 重试次数
         */
        fun retryCount(count: Int) = apply {
            require(count >= 0) { "Retry count must be >= 0" }
            this.retryCount = count
        }

        /**
         * 设置重试延迟（毫秒）
         *
         * @param delay 延迟时间
         */
        fun retryDelay(delay: Long) = apply {
            require(delay >= 0) { "Retry delay must be >= 0" }
            this.retryDelay = delay
        }

        /**
         * 设置重试条件
         *
         * @param predicate 重试条件判断函数
         */
        fun retryOn(predicate: (IOException) -> Boolean) = apply {
            this.retryOn = predicate
        }

        /**
         * 构建 RetryInterceptor 实例
         *
         * @return RetryInterceptor 实例
         */
        fun build(): RetryInterceptor {
            return RetryInterceptor(retryCount, retryDelay, retryOn)
        }
    }
}