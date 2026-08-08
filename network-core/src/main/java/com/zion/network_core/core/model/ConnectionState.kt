package com.zion.network_core.core.model

/**
 * 连接状态密封类（用于 Socket、WebSocket 等）
 */
sealed class ConnectionState {

    /**
     * 已断开连接
     */
    object Disconnected : ConnectionState()

    /**
     * 连接中
     */
    object Connecting : ConnectionState()

    /**
     * 已连接
     */
    object Connected : ConnectionState()

    /**
     * 重连中
     *
     * @param attempt 当前重试次数
     * @param maxAttempts 最大重试次数
     */
    data class Reconnecting(val attempt: Int, val maxAttempts: Int = 3) : ConnectionState()

    /**
     * 连接错误
     *
     * @param exception 异常信息
     */
    data class Error(val exception: Throwable) : ConnectionState()

    /**
     * 是否已连接
     */
    val isConnected: Boolean
        get() = this is Connected

    /**
     * 是否正在连接
     */
    val isConnecting: Boolean
        get() = this is Connecting || this is Reconnecting

    /**
     * 是否已断开
     */
    val isDisconnected: Boolean
        get() = this is Disconnected || this is Error
}