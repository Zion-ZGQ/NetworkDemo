package com.zion.network_monitor

import com.zion.network_core.core.model.NetworkType

/**
 * 网络信息数据类
 *
 * @param type 网络类型
 * @param isConnected 是否已连接
 * @param isAvailable 是否可用
 * @param isRoaming 是否漫游
 * @param typeName 网络类型名称
 * @param subtypeName 子类型名称
 */
data class NetworkInfo(
    val type: NetworkType = NetworkType.UNKNOWN,
    val isConnected: Boolean = false,
    val isAvailable: Boolean = false,
    val isRoaming: Boolean = false,
    val typeName: String? = null,
    val subtypeName: String? = null
) {
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

    /**
     * 网络类型描述
     */
    val typeDescription: String
        get() = when (type) {
            NetworkType.WIFI -> "WiFi"
            NetworkType.MOBILE_2G -> "2G"
            NetworkType.MOBILE_3G -> "3G"
            NetworkType.MOBILE_4G -> "4G"
            NetworkType.MOBILE_5G -> "5G"
            NetworkType.ETHERNET -> "Ethernet"
            NetworkType.UNKNOWN -> "Unknown"
        }
}