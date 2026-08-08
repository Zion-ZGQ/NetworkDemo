package com.zion.network_http.api

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/**
 * HTTP 请求构建器
 *
 * 用于构建 HTTP 请求，支持链式调用
 *
 * 使用示例：
 * ```kotlin
 * val request = HttpRequest.Builder()
 *     .url("https://api.example.com/users")
 *     .get()
 *     .addHeader("Authorization", "Bearer token")
 *     .build()
 * ```
 */
class HttpRequest private constructor(
    builder: Builder
) {
    /**
     * 请求 URL
     */
    val url: String = builder.url

    /**
     * 请求方法
     */
    val method: HttpMethod = builder.method

    /**
     * 请求头
     */
    val headers: Map<String, String> = builder.headers.toMap()

    /**
     * 请求参数（用于 GET 请求）
     */
    val params: Map<String, String> = builder.params.toMap()

    /**
     * 请求体
     */
    val body: RequestBody? = builder.body

    /**
     * 请求标签（用于取消请求）
     */
    val tag: String? = builder.tag

    /**
     * 缓存策略
     */
    val cachePolicy: CachePolicy = builder.cachePolicy

    /**
     * 连接超时时间（秒）
     */
    val connectTimeout: Long? = builder.connectTimeout

    /**
     * 读取超时时间（秒）
     */
    val readTimeout: Long? = builder.readTimeout

    /**
     * 写入超时时间（秒）
     */
    val writeTimeout: Long? = builder.writeTimeout

    /**
     * Builder 类
     */
    class Builder {
        internal var url: String = ""
            private set

        internal var method: HttpMethod = HttpMethod.GET
            private set

        internal var headers: MutableMap<String, String> = mutableMapOf()
            private set

        internal var params: MutableMap<String, String> = mutableMapOf()
            private set

        internal var body: RequestBody? = null
            private set

        internal var tag: String? = null
            private set

        internal var cachePolicy: CachePolicy = CachePolicy.DEFAULT
            private set

        internal var connectTimeout: Long? = null
            private set

        internal var readTimeout: Long? = null
            private set

        internal var writeTimeout: Long? = null
            private set

        /**
         * 设置请求 URL
         *
         * @param url 请求 URL
         */
        fun url(url: String) = apply {
            this.url = url
        }

        /**
         * 设置 GET 请求
         */
        fun get() = apply {
            this.method = HttpMethod.GET
            this.body = null
        }

        /**
         * 设置 POST 请求
         *
         * @param body 请求体
         */
        fun post(body: RequestBody? = null) = apply {
            this.method = HttpMethod.POST
            this.body = body
        }

        /**
         * 设置 POST JSON 请求
         *
         * @param json JSON 字符串
         */
        fun postJson(json: String) = apply {
            this.method = HttpMethod.POST
            this.body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        }

        /**
         * 设置 PUT 请求
         *
         * @param body 请求体
         */
        fun put(body: RequestBody? = null) = apply {
            this.method = HttpMethod.PUT
            this.body = body
        }

        /**
         * 设置 DELETE 请求
         *
         * @param body 请求体
         */
        fun delete(body: RequestBody? = null) = apply {
            this.method = HttpMethod.DELETE
            this.body = body
        }

        /**
         * 设置 PATCH 请求
         *
         * @param body 请求体
         */
        fun patch(body: RequestBody? = null) = apply {
            this.method = HttpMethod.PATCH
            this.body = body
        }

        /**
         * 设置 HEAD 请求
         */
        fun head() = apply {
            this.method = HttpMethod.HEAD
            this.body = null
        }

        /**
         * 添加请求头
         *
         * @param name 请求头名称
         * @param value 请求头值
         */
        fun addHeader(name: String, value: String) = apply {
            this.headers[name] = value
        }

        /**
         * 设置请求头
         *
         * @param headers 请求头 Map
         */
        fun headers(headers: Map<String, String>) = apply {
            this.headers.clear()
            this.headers.putAll(headers)
        }

        /**
         * 添加请求参数
         *
         * @param name 参数名
         * @param value 参数值
         */
        fun addParam(name: String, value: String) = apply {
            this.params[name] = value
        }

        /**
         * 设置请求参数
         *
         * @param params 参数 Map
         */
        fun params(params: Map<String, String>) = apply {
            this.params.clear()
            this.params.putAll(params)
        }

        /**
         * 设置请求体
         *
         * @param body 请求体
         */
        fun body(body: RequestBody) = apply {
            this.body = body
        }

        /**
         * 设置请求标签
         *
         * @param tag 标签
         */
        fun tag(tag: String) = apply {
            this.tag = tag
        }

        /**
         * 设置缓存策略
         *
         * @param policy 缓存策略
         */
        fun cachePolicy(policy: CachePolicy) = apply {
            this.cachePolicy = policy
        }

        /**
         * 设置连接超时时间
         *
         * @param timeout 超时时间（秒）
         */
        fun connectTimeout(timeout: Long) = apply {
            this.connectTimeout = timeout
        }

        /**
         * 设置读取超时时间
         *
         * @param timeout 超时时间（秒）
         */
        fun readTimeout(timeout: Long) = apply {
            this.readTimeout = timeout
        }

        /**
         * 设置写入超时时间
         *
         * @param timeout 超时时间（秒）
         */
        fun writeTimeout(timeout: Long) = apply {
            this.writeTimeout = timeout
        }

        /**
         * 设置所有超时时间
         *
         * @param timeout 超时时间（秒）
         */
        fun timeout(timeout: Long) = apply {
            this.connectTimeout = timeout
            this.readTimeout = timeout
            this.writeTimeout = timeout
        }

        /**
         * 构建 HttpRequest 实例
         *
         * @return HttpRequest 实例
         */
        fun build(): HttpRequest {
            if (url.isBlank()) {
                throw IllegalArgumentException("URL cannot be empty")
            }
            return HttpRequest(this)
        }
    }

    /**
     * HTTP 请求方法枚举
     */
    enum class HttpMethod {
        /**
         * GET 请求
         */
        GET,

        /**
         * POST 请求
         */
        POST,

        /**
         * PUT 请求
         */
        PUT,

        /**
         * DELETE 请求
         */
        DELETE,

        /**
         * PATCH 请求
         */
        PATCH,

        /**
         * HEAD 请求
         */
        HEAD,

        /**
         * OPTIONS 请求
         */
        OPTIONS
    }

    /**
     * 缓存策略
     */
    enum class CachePolicy {
        /**
         * 默认策略（遵循服务器缓存控制）
         */
        DEFAULT,

        /**
         * 仅使用网络
         */
        NETWORK_ONLY,

        /**
         * 仅使用缓存
         */
        CACHE_ONLY,

        /**
         * 优先使用缓存，缓存不存在时使用网络
         */
        CACHE_FIRST,

        /**
         * 优先使用网络，网络失败时使用缓存
         */
        NETWORK_FIRST
    }

    companion object {
        /**
         * 创建 GET 请求
         *
         * @param url 请求 URL
         * @return HttpRequest 实例
         */
        fun get(url: String): Builder {
            return Builder().url(url).get()
        }

        /**
         * 创建 POST 请求
         *
         * @param url 请求 URL
         * @return HttpRequest 实例
         */
        fun post(url: String): Builder {
            return Builder().url(url).post()
        }
    }
}