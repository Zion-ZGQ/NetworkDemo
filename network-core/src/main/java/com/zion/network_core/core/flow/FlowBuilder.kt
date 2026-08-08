package com.zion.network_core.core.flow

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Flow 构建工具类
 *
 * 提供各种 Flow 创建的便捷方法
 */
object FlowBuilder {

    /**
     * 将回调式 API 转换为 Flow
     *
     * @param register 注册回调
     * @param unregister 注销回调
     */
    inline fun <T> callbackFlow(
        crossinline register: (callback: (T) -> Unit) -> Unit,
        crossinline unregister: (callback: (T) -> Unit) -> Unit
    ): Flow<T> = callbackFlow {
        val callback: (T) -> Unit = { value ->
            trySend(value)
        }

        register(callback)

        awaitClose {
            unregister(callback)
        }
    }

    /**
     * 创建带初始值的回调 Flow
     *
     * @param initialValue 初始值
     * @param register 注册回调
     * @param unregister 注销回调
     */
    inline fun <T> callbackFlowWithInitial(
        initialValue: T,
        crossinline register: (callback: (T) -> Unit) -> Unit,
        crossinline unregister: (callback: (T) -> Unit) -> Unit
    ): Flow<T> = callbackFlow {
        // 先发送初始值
        trySend(initialValue)

        val callback: (T) -> Unit = { value ->
            trySend(value)
        }

        register(callback)

        awaitClose {
            unregister(callback)
        }
    }

    /**
     * 创建带状态变化的回调 Flow
     *
     * @param register 注册回调
     * @param unregister 注销回调
     */
    inline fun <T> stateFlow(
        crossinline register: (callback: (T) -> Unit) -> Unit,
        crossinline unregister: (callback: (T) -> Unit) -> Unit
    ): Flow<T> = callbackFlow {
        val callback: (T) -> Unit = { value ->
            trySend(value)
        }

        register(callback)

        awaitClose {
            unregister(callback)
        }
    }
}