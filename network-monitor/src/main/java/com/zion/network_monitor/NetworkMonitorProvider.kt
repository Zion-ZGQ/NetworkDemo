package com.zion.network_monitor

import com.zion.network_core.api.NetworkKit

/**
 * NetworkMonitor 提供者
 *
 * 用于获取 NetworkMonitor 实例
 */
object NetworkMonitorProvider {

    private var _monitor: NetworkMonitor? = null

    /**
     * 获取 NetworkMonitor 实例（单例）
     */
    val monitor: NetworkMonitor
        get() {
            NetworkKit.checkInitialized()

            if (_monitor == null) {
                _monitor = NetworkStateManager(NetworkKit.context)
            }

            return _monitor!!
        }

    /**
     * 注册网络监听（建议在 Application 中调用）
     */
    fun register() {
        monitor.register()
    }

    /**
     * 注销网络监听
     */
    fun unregister() {
        _monitor?.unregister()
    }
}