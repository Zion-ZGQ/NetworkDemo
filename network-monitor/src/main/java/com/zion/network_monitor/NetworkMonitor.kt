package com.zion.network_monitor

import com.zion.network_core.core.model.NetworkState
import kotlinx.coroutines.flow.StateFlow

/**
 * 网络监测器接口
 */
interface NetworkMonitor {

    /**
     * 网络状态（StateFlow）
     */
    val networkState: StateFlow<NetworkState>

    /**
     * 监听网络信息
     */
    fun observeNetworkInfo(): kotlinx.coroutines.flow.Flow<NetworkInfo>

    /**
     * 监听网络可用性
     */
    fun observeAvailability(): kotlinx.coroutines.flow.Flow<Boolean>

    /**
     * 获取当前网络状态
     */
    fun getCurrentState(): NetworkState

    /**
     * 是否已连接
     */
    fun isConnected(): Boolean

    /**
     * 是否可用
     */
    fun isAvailable(): Boolean

    /**
     * 注册监听（需要在 Application 中调用）
     */
    fun register()

    /**
     * 注销监听
     */
    fun unregister()
}