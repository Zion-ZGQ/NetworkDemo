package com.zion.network_http.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * 请求头拦截器
 *
 * 用于统一添加请求头，支持添加公共请求头（如 Token、User-Agent 等）
 *
 * 使用示例：
 * ```kotlin
 * val interceptor = HeaderInterceptor.Builder()
 *     .addHeader("Authorization", "Bearer token")
 *     .addHeader("User-Agent", "MyApp/1.0")
 *     .build()
 * ```
 */
class HeaderInterceptor private constructor(
    private val headers: Map<String, String>
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 如果没有需要添加的头，直接执行请求
        if (headers.isEmpty()) {
            return chain.proceed(originalRequest)
        }

        // 构建新的请求
        val builder = originalRequest.newBuilder()
        headers.forEach { (name, value) ->
            // 检查请求中是否已经存在该头
            if (originalRequest.header(name) == null) {
                builder.addHeader(name, value)
            }
        }

        val newRequest = builder.build()
        return chain.proceed(newRequest)
    }

    /**
     * Builder 类
     */
    class Builder {
        private val headers: MutableMap<String, String> = mutableMapOf()

        /**
         * 添加请求头
         *
         * @param name 请求头名称
         * @param value 请求头值
         */
        fun addHeader(name: String, value: String) = apply {
            headers[name] = value
        }

        /**
         * 添加多个请求头
         *
         * @param headers 请求头 Map
         */
        fun addHeaders(headers: Map<String, String>) = apply {
            this.headers.putAll(headers)
        }

        /**
         * 移除请求头
         *
         * @param name 请求头名称
         */
        fun removeHeader(name: String) = apply {
            headers.remove(name)
        }

        /**
         * 清空所有请求头
         */
        fun clearHeaders() = apply {
            headers.clear()
        }

        /**
         * 构建 HeaderInterceptor 实例
         *
         * @return HeaderInterceptor 实例
         */
        fun build(): HeaderInterceptor {
            return HeaderInterceptor(headers.toMap())
        }
    }
}