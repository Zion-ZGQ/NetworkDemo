package com.zion.network_core.api

import android.annotation.SuppressLint
import android.content.Context
import com.zion.network_core.core.executor.DefaultDispatcherProvider
import com.zion.network_core.core.executor.DispatcherProvider
import com.zion.network_core.core.executor.NetworkExecutor
import com.zion.network_core.core.logger.Logger
import com.zion.network_core.core.logger.NetworkLogger

/**
 * 网络库主入口单例
 *
 * 所有网络功能的访问入口
 *
 * 注意：NetworkKit 持有的是 ApplicationContext，不会导致内存泄漏。
 * 在 init() 方法中会自动将传入的 Context 转换为 ApplicationContext。
 */
@SuppressLint("StaticFieldLeak")
object NetworkKit {

    private const val TAG = "NetworkKit"

    /**
     * 全局配置
     */
    lateinit var config: NetworkConfig
        private set

    /**
     * 日志系统
     */
    lateinit var logger: Logger
        private set

    /**
     * 协程调度器提供者
     */
    lateinit var dispatcherProvider: DispatcherProvider
        private set

    /**
     * 应用上下文
     */
    lateinit var context: Context
        private set

    /**
     * 是否已初始化
     */
    var isInitialized: Boolean = false
        private set

    /**
     * 初始化网络库
     *
     * @param context 应用上下文
     * @param config 全局配置
     */
    fun init(context: Context, config: NetworkConfig = NetworkConfig()) {
        if (isInitialized) {
            logger.w(TAG, "NetworkKit has already been initialized")
            return
        }

        this.context = context.applicationContext
        this.config = config

        // 初始化日志系统
        this.logger = config.logger ?: NetworkLogger()
        this.logger.level = config.logLevel

        // 初始化协程调度器
        this.dispatcherProvider = config.dispatcherProvider ?: DefaultDispatcherProvider()
        NetworkExecutor.dispatcherProvider = this.dispatcherProvider

        isInitialized = true

        logger.i(TAG, "NetworkKit initialized successfully")
    }

    /**
     * 检查是否已初始化
     */
    fun checkInitialized() {
        if (!isInitialized) {
            throw IllegalStateException("NetworkKit not initialized, please call NetworkKit.init() first")
        }
    }

    /**
     * 使用 DSL 风格初始化
     */
    inline fun init(context: Context, block: NetworkConfig.Builder.() -> Unit) {
        val config = NetworkConfig.Builder().apply(block).build()
        init(context, config)
    }
}