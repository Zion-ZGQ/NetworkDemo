package com.zion.network_http.config

import com.zion.network_http.converter.Converter
import com.zion.network_http.converter.GsonConverter
import okhttp3.Interceptor
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * HTTP 客户端配置类
 *
 * 用于配置 HTTP 客户端的各种参数，包括超时时间、缓存、拦截器等
 *
 * 使用示例：
 * ```
 * val config = HttpConfig.Builder()
 *     .connectTimeout(30)
 *     .addInterceptor(loggingInterceptor)
 *     .build()
 * ```
 */
class HttpConfig private constructor(
    builder: Builder
) {
    /**
     * 连接超时时间（单位：秒）
     */
    val connectTimeout: Long = builder.connectTimeout

    /**
     * 读取超时时间（单位：秒）
     */
    val readTimeout: Long = builder.readTimeout

    /**
     * 写入超时时间（单位：秒）
     */
    val writeTimeout: Long = builder.writeTimeout

    /**
     * 是否启用缓存
     */
    val enableCache: Boolean = builder.enableCache

    /**
     * 缓存目录
     */
    val cacheDir: File? = builder.cacheDir

    /**
     * 缓存大小（单位：字节）
     */
    val cacheSize: Long = builder.cacheSize

    /**
     * 是否启用日志
     */
    val enableLog: Boolean = builder.enableLog

    /**
     * 日志标签
     */
    val logTag: String = builder.logTag

    /**
     * 是否重试
     */
    val enableRetry: Boolean = builder.enableRetry

    /**
     * 重试次数
     */
    val retryCount: Int = builder.retryCount

    /**
     * 重试延迟时间（单位：毫秒）
     */
    val retryDelay: Long = builder.retryDelay

    /**
     * 拦截器列表
     */
    val interceptors: List<Interceptor> = builder.interceptors

    /**
     * 网络拦截器列表
     */
    val networkInterceptors: List<Interceptor> = builder.networkInterceptors

    /**
     * 数据转换器
     */
    val converter: Converter = builder.converter

    /**
     * 基础 URL
     */
    val baseUrl: String? = builder.baseUrl

    /**
     * 是否启用 SSL 证书验证（开发环境可关闭）
     */
    val enableSSL: Boolean = builder.enableSSL

    /**
     * Builder 类，用于构建 HttpConfig 实例
     */
    class Builder {
        /**
         * 连接超时时间（单位：秒）
         */
        var connectTimeout: Long = 30L
            private set

        /**
         * 读取超时时间（单位：秒）
         */
        var readTimeout: Long = 30L
            private set

        /**
         * 写入超时时间（单位：秒）
         */
        var writeTimeout: Long = 30L
            private set

        /**
         * 是否启用缓存
         */
        var enableCache: Boolean = false
            private set

        /**
         * 缓存目录
         */
        var cacheDir: File? = null
            private set

        /**
         * 缓存大小（单位：字节）
         */
        var cacheSize: Long = 10 * 1024 * 1024L // 默认 10MB
            private set

        /**
         * 是否启用日志
         */
        var enableLog: Boolean = true
            private set

        /**
         * 日志标签
         */
        var logTag: String = "HttpClient"
            private set

        /**
         * 是否重试
         */
        var enableRetry: Boolean = true
            private set

        /**
         * 重试次数
         */
        var retryCount: Int = 3
            private set

        /**
         * 重试延迟时间（单位：毫秒）
         */
        var retryDelay: Long = 1000L
            private set

        /**
         * 拦截器列表
         */
        var interceptors: MutableList<Interceptor> = mutableListOf()
            private set

        /**
         * 网络拦截器列表
         */
        var networkInterceptors: MutableList<Interceptor> = mutableListOf()
            private set

        /**
         * 数据转换器
         */
        var converter: Converter = GsonConverter()
            private set

        /**
         * 基础 URL
         */
        var baseUrl: String? = null
            private set

        /**
         * 是否启用 SSL 证书验证
         */
        var enableSSL: Boolean = true
            private set

        /**
         * 设置连接超时时间
         *
         * @param timeout 超时时间（单位：秒）
         */
        fun connectTimeout(timeout: Long) = apply {
            this.connectTimeout = timeout
        }

        /**
         * 设置读取超时时间
         *
         * @param timeout 超时时间（单位：秒）
         */
        fun readTimeout(timeout: Long) = apply {
            this.readTimeout = timeout
        }

        /**
         * 设置写入超时时间
         *
         * @param timeout 超时时间（单位：秒）
         */
        fun writeTimeout(timeout: Long) = apply {
            this.writeTimeout = timeout
        }

        /**
         * 设置所有超时时间
         *
         * @param timeout 超时时间（单位：秒）
         */
        fun timeout(timeout: Long) = apply {
            this.connectTimeout = timeout
            this.readTimeout = timeout
            this.writeTimeout = timeout
        }

        /**
         * 启用缓存
         *
         * @param cacheDir 缓存目录
         * @param cacheSize 缓存大小（单位：字节）
         */
        fun enableCache(cacheDir: File, cacheSize: Long = 10 * 1024 * 1024L) = apply {
            this.enableCache = true
            this.cacheDir = cacheDir
            this.cacheSize = cacheSize
        }

        /**
         * 禁用缓存
         */
        fun disableCache() = apply {
            this.enableCache = false
            this.cacheDir = null
        }

        /**
         * 设置日志开关
         *
         * @param enable 是否启用
         * @param tag 日志标签
         */
        fun enableLog(enable: Boolean = true, tag: String = "HttpClient") = apply {
            this.enableLog = enable
            this.logTag = tag
        }

        /**
         * 设置重试配置
         *
         * @param enable 是否启用重试
         * @param count 重试次数
         * @param delay 重试延迟时间（单位：毫秒）
         */
        fun retry(enable: Boolean = true, count: Int = 3, delay: Long = 1000L) = apply {
            this.enableRetry = enable
            this.retryCount = count
            this.retryDelay = delay
        }

        /**
         * 添加拦截器
         *
         * @param interceptor 拦截器实例
         */
        fun addInterceptor(interceptor: Interceptor) = apply {
            this.interceptors.add(interceptor)
        }

        /**
         * 添加网络拦截器
         *
         * @param interceptor 拦截器实例
         */
        fun addNetworkInterceptor(interceptor: Interceptor) = apply {
            this.networkInterceptors.add(interceptor)
        }

        /**
         * 设置数据转换器
         *
         * @param converter 转换器实例
         */
        fun converter(converter: Converter) = apply {
            this.converter = converter
        }

        /**
         * 设置基础 URL
         *
         * @param url 基础 URL
         */
        fun baseUrl(url: String) = apply {
            this.baseUrl = url
        }

        /**
         * 设置 SSL 证书验证开关
         *
         * @param enable 是否启用
         */
        fun enableSSL(enable: Boolean) = apply {
            this.enableSSL = enable
        }

        /**
         * 构建 HttpConfig 实例
         *
         * @return HttpConfig 实例
         */
        fun build(): HttpConfig {
            return HttpConfig(this)
        }
    }

    companion object {
        /**
         * 默认配置
         */
        val DEFAULT = Builder().build()
    }
}