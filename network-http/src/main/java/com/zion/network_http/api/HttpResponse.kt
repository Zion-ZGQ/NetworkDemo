package com.zion.network_http.api

import okhttp3.Response
import okhttp3.ResponseBody

/**
 * HTTP 响应封装
 *
 * 封装了 HTTP 响应的基本信息，包括状态码、响应头、响应体等
 *
 * @param T 响应体数据类型
 */
class HttpResponse<T> private constructor(
    builder: Builder<T>
) {
    /**
     * 响应状态码
     */
    val code: Int = builder.code

    /**
     * 响应消息
     */
    val message: String = builder.message

    /**
     * 响应头
     */
    val headers: Map<String, List<String>> = builder.headers

    /**
     * 响应体
     */
    val body: T? = builder.body

    /**
     * 响应时间（毫秒）
     */
    val responseTime: Long = builder.responseTime

    /**
     * 请求 URL
     */
    val requestUrl: String = builder.requestUrl

    /**
     * 是否成功（状态码在 200-299 之间）
     */
    val isSuccess: Boolean
        get() = code in 200..299

    /**
     * 是否重定向（状态码在 300-399 之间）
     */
    val isRedirect: Boolean
        get() = code in 300..399

    /**
     * 是否客户端错误（状态码在 400-499 之间）
     */
    val isClientError: Boolean
        get() = code in 400..499

    /**
     * 是否服务器错误（状态码在 500-599 之间）
     */
    val isServerError: Boolean
        get() = code in 500..599

    /**
     * 获取指定名称的响应头
     *
     * @param name 响应头名称
     * @return 响应头值列表，如果不存在则返回空列表
     */
    fun getHeader(name: String): List<String> {
        return headers[name] ?: emptyList()
    }

    /**
     * 获取指定名称的第一个响应头值
     *
     * @param name 响应头名称
     * @return 响应头值，如果不存在则返回 null
     */
    fun getFirstHeader(name: String): String? {
        return headers[name]?.firstOrNull()
    }

    /**
     * Builder 类
     */
    class Builder<T> {
        internal var code: Int = 0
            private set

        internal var message: String = ""
            private set

        internal var headers: Map<String, List<String>> = emptyMap()
            private set

        internal var body: T? = null
            private set

        internal var responseTime: Long = 0L
            private set

        internal var requestUrl: String = ""
            private set

        /**
         * 设置响应状态码
         *
         * @param code 状态码
         */
        fun code(code: Int) = apply {
            this.code = code
        }

        /**
         * 设置响应消息
         *
         * @param message 响应消息
         */
        fun message(message: String) = apply {
            this.message = message
        }

        /**
         * 设置响应头
         *
         * @param headers 响应头 Map
         */
        fun headers(headers: Map<String, List<String>>) = apply {
            this.headers = headers
        }

        /**
         * 设置响应体
         *
         * @param body 响应体
         */
        fun body(body: T?) = apply {
            this.body = body
        }

        /**
         * 设置响应时间
         *
         * @param time 响应时间（毫秒）
         */
        fun responseTime(time: Long) = apply {
            this.responseTime = time
        }

        /**
         * 设置请求 URL
         *
         * @param url 请求 URL
         */
        fun requestUrl(url: String) = apply {
            this.requestUrl = url
        }

        /**
         * 从 OkHttp Response 构建
         *
         * @param response OkHttp Response
         * @param body 转换后的响应体
         */
        fun fromOkHttpResponse(response: Response, body: T?) = apply {
            this.code = response.code
            this.message = response.message
            this.headers = response.headers.toMultimap()
            this.body = body
            this.requestUrl = response.request.url.toString()
        }

        /**
         * 构建 HttpResponse 实例
         *
         * @return HttpResponse 实例
         */
        fun build(): HttpResponse<T> {
            return HttpResponse(this)
        }
    }

    override fun toString(): String {
        return "HttpResponse(code=$code, message='$message', body=$body, responseTime=$responseTime ms)"
    }

    companion object {
        /**
         * 从 OkHttp Response 创建 HttpResponse
         *
         * @param response OkHttp Response
         * @param body 转换后的响应体
         * @return HttpResponse 实例
         */
        fun <T> from(response: Response, body: T?): HttpResponse<T> {
            return Builder<T>()
                .fromOkHttpResponse(response, body)
                .responseTime(response.receivedResponseAtMillis - response.sentRequestAtMillis)
                .build()
        }
    }
}