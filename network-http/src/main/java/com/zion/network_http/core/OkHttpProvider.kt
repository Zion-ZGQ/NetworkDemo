package com.zion.network_http.core

import android.annotation.SuppressLint
import com.zion.network_core.api.NetworkKit
import com.zion.network_http.config.HttpConfig
import com.zion.network_http.interceptor.LoggingInterceptor
import com.zion.network_http.interceptor.RetryInterceptor
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * OkHttp 实例管理器
 *
 * 使用单例模式管理 OkHttpClient 实例，支持多配置管理
 *
 * 使用示例：
 * ```kotlin
 * val client = OkHttpProvider.getOrCreateClient(config)
 * ```
 */
object OkHttpProvider {

    private const val TAG = "OkHttpProvider"

    /**
     * 客户端实例缓存
     */
    private val clientCache = ConcurrentHashMap<String, OkHttpClient>()

    /**
     * 默认客户端实例
     */
    @Volatile
    private var defaultClient: OkHttpClient? = null

    /**
     * 获取或创建 OkHttpClient 实例
     *
     * @param config HTTP 配置
     * @return OkHttpClient 实例
     */
    fun getOrCreateClient(config: HttpConfig): OkHttpClient {
        // 如果使用默认配置，返回默认客户端
        if (config == HttpConfig.DEFAULT) {
            return getDefaultClient()
        }

        // 根据配置生成缓存键
        val cacheKey = generateCacheKey(config)

        // 从缓存中获取或创建新实例
        return clientCache.getOrPut(cacheKey) {
            createClient(config)
        }
    }

    /**
     * 获取默认 OkHttpClient 实例
     *
     * @return 默认 OkHttpClient 实例
     */
    fun getDefaultClient(): OkHttpClient {
        return defaultClient ?: synchronized(this) {
            defaultClient ?: createClient(HttpConfig.DEFAULT).also {
                defaultClient = it
            }
        }
    }

    /**
     * 创建 OkHttpClient 实例
     *
     * @param config HTTP 配置
     * @return OkHttpClient 实例
     */
    private fun createClient(config: HttpConfig): OkHttpClient {
        NetworkKit.checkInitialized()

        val builder = OkHttpClient.Builder()

        // 配置超时时间
        builder.connectTimeout(config.connectTimeout, TimeUnit.SECONDS)
        builder.readTimeout(config.readTimeout, TimeUnit.SECONDS)
        builder.writeTimeout(config.writeTimeout, TimeUnit.SECONDS)

        // 配置缓存
        if (config.enableCache && config.cacheDir != null) {
            val cache = Cache(config.cacheDir, config.cacheSize)
            builder.cache(cache)
        }

        // 配置日志拦截器
        if (config.enableLog) {
            val loggingInterceptor = LoggingInterceptor(
                if (config.enableLog) LoggingInterceptor.Level.BODY else LoggingInterceptor.Level.NONE
            )
            builder.addInterceptor(loggingInterceptor)
        }

        // 配置重试拦截器
        if (config.enableRetry) {
            val retryInterceptor = RetryInterceptor.Builder()
                .retryCount(config.retryCount)
                .retryDelay(config.retryDelay)
                .build()
            builder.addInterceptor(retryInterceptor)
        }

        // 添加自定义拦截器
        config.interceptors.forEach { interceptor ->
            builder.addInterceptor(interceptor)
        }

        // 添加网络拦截器
        config.networkInterceptors.forEach { interceptor ->
            builder.addNetworkInterceptor(interceptor)
        }

        // 配置 SSL（开发环境可禁用）
        if (!config.enableSSL) {
            configureUnsafeSsl(builder)
        }

        // 配置连接池和重试
        builder.retryOnConnectionFailure(true)

        NetworkKit.logger.i(TAG, "OkHttpClient created with config: timeout=${config.connectTimeout}s, cache=${config.enableCache}")

        return builder.build()
    }

    /**
     * 配置不安全的 SSL（仅用于开发环境）
     */
    @SuppressLint("TrustAllX509TrustManager", "BadHostnameVerifier")
    private fun configureUnsafeSsl(builder: OkHttpClient.Builder) {
        try {
            // 创建信任所有证书的 TrustManager
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            // 安装信任所有证书的 TrustManager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())

            // 配置 SSL Socket Factory
            builder.sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)

            // 配置 HostnameVerifier，信任所有主机名
            builder.hostnameVerifier { _, _ -> true }

            NetworkKit.logger.w(TAG, "Unsafe SSL configured (development mode only)")
        } catch (e: Exception) {
            NetworkKit.logger.e(TAG, "Failed to configure unsafe SSL", e)
        }
    }

    /**
     * 生成缓存键
     *
     * @param config HTTP 配置
     * @return 缓存键
     */
    private fun generateCacheKey(config: HttpConfig): String {
        return "${config.connectTimeout}_${config.readTimeout}_${config.writeTimeout}_${config.enableCache}_${config.enableLog}_${config.enableSSL}_${config.interceptors.size}_${config.networkInterceptors.size}"
    }

    /**
     * 清除所有缓存的客户端实例
     */
    fun clearCache() {
        clientCache.clear()
        defaultClient = null
        NetworkKit.logger.i(TAG, "Client cache cleared")
    }

    /**
     * 移除指定配置的客户端实例
     *
     * @param config HTTP 配置
     */
    fun removeClient(config: HttpConfig) {
        val cacheKey = generateCacheKey(config)
        clientCache.remove(cacheKey)
        NetworkKit.logger.i(TAG, "Client removed for key: $cacheKey")
    }
}