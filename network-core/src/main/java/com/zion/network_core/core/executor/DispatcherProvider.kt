package com.zion.network_core.core.executor

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * 协程调度器提供者接口
 *
 * 用于统一管理协程调度器，方便测试时替换
 */
interface DispatcherProvider {
    /**
     * 主线程调度器
     */
    val main: CoroutineDispatcher

    /**
     * IO 调度器（网络请求、文件操作等）
     */
    val io: CoroutineDispatcher

    /**
     * 默认计算调度器（CPU 密集型任务）
     */
    val default: CoroutineDispatcher
}

/**
 * 默认的协程调度器实现
 */
class DefaultDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
}