package com.zion.network_core.core.executor

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * 网络库协程执行器
 *
 * 提供统一的协程作用域管理
 */
object NetworkExecutor : CoroutineScope {

    /**
     * 协程调度器提供者
     */
    var dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider()

    /**
     * 协程异常处理器
     */
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        // 这里可以集成日志系统
        throwable.printStackTrace()
    }

    /**
     * 协程上下文
     */
    override val coroutineContext: CoroutineContext
        get() = dispatcherProvider.io + SupervisorJob() + exceptionHandler

    /**
     * 在 IO 线程执行任务
     */
    fun launchIO(block: suspend CoroutineScope.() -> Unit): Job {
        return launch(dispatcherProvider.io, block = block)
    }

    /**
     * 在主线程执行任务
     */
    fun launchMain(block: suspend CoroutineScope.() -> Unit): Job {
        return launch(dispatcherProvider.main, block = block)
    }

    /**
     * 在默认线程执行任务
     */
    fun launchDefault(block: suspend CoroutineScope.() -> Unit): Job {
        return launch(dispatcherProvider.default, block = block)
    }

    /**
     * 取消所有任务
     */
    fun cancelAll() {
        coroutineContext.cancel()
    }
}