package com.zion.network_core.core.model

/**
 * 网络状态密封类
 */
sealed class NetworkState {

    /**
     * 空闲状态（未检测）
     */
    object Idle : NetworkState()

    /**
     * 连接中
     */
    object Connecting : NetworkState()

    /**
     * 已连接
     *
     * @param type 网络类型
     */
    data class Connected(val type: NetworkType) : NetworkState() {
        /**
         * 是否为 WiFi
         */
        val isWifi: Boolean
            get() = type == NetworkType.WIFI

        /**
         * 是否为移动网络
         */
        val isMobile: Boolean
            get() = type in listOf(
                NetworkType.MOBILE_2G,
                NetworkType.MOBILE_3G,
                NetworkType.MOBILE_4G,
                NetworkType.MOBILE_5G
            )

        /**
         * 是否为 5G 网络
         */
        val is5G: Boolean
            get() = type == NetworkType.MOBILE_5G
    }

    /**
     * 已断开连接
     *
     * @param reason 断开原因
     */
    data class Disconnected(val reason: String? = null) : NetworkState()

    /**
     * 是否已连接
     */
    val isConnected: Boolean
        get() = this is Connected

    /**
     * 是否已断开
     */
    val isDisconnected: Boolean
        get() = this is Disconnected
}