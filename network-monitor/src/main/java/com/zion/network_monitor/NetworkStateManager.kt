package com.zion.network_monitor

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import com.zion.network_core.core.model.NetworkState
import com.zion.network_core.core.model.NetworkType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 网络状态管理器实现
 *
 * 使用 Android ConnectivityManager 监听网络状态
 * 适配 Android 5.0+ (API 21+)
 */
class NetworkStateManager(
    private val context: Context
) : NetworkMonitor {

    private val _networkState = MutableStateFlow<NetworkState>(NetworkState.Idle)
    override val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()

    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    // 缓存 5G 能力常量（通过反射获取）
    private var netCapabilityMmsecure: Int = -1

    init {
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        init5GCapability()
    }

    /**
     * 初始化 5G 能力常量（兼容 Android 5.0+）
     */
    private fun init5GCapability() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                // Android 11+ 通过反射获取 NET_CAPABILITY_MMSECURE
                val field = NetworkCapabilities::class.java.getDeclaredField("NET_CAPABILITY_MMSECURE")
                netCapabilityMmsecure = field.getInt(null)
            } catch (e: Exception) {
                // 反射失败，使用带宽估算
                netCapabilityMmsecure = -1
            }
        }
    }

    override fun register() {
        if (networkCallback != null) return

        val cm = connectivityManager ?: return

        // Android 5.0+ (API 21+) 都支持 NetworkCallback
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            registerNetworkCallback(cm)
        } else {
            // 低于 Android 5.0 的情况（理论上不会走到这里，因为 minSdk = 21）
            updateNetworkStateLegacy(cm)
        }
    }

    /**
     * 注册网络回调（API 21+）
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun registerNetworkCallback(cm: ConnectivityManager) {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                updateNetworkState(network, true)
            }

            override fun onLost(network: Network) {
                _networkState.value = NetworkState.Disconnected("Network lost")
            }

            override fun onUnavailable() {
                _networkState.value = NetworkState.Disconnected("Network unavailable")
            }
        }

        cm.registerNetworkCallback(networkRequest, networkCallback!!)

        // 获取当前网络状态
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0+ 使用 getActiveNetwork()
            val activeNetwork = cm.activeNetwork
            if (activeNetwork != null) {
                updateNetworkState(activeNetwork, true)
            } else {
                _networkState.value = NetworkState.Disconnected("No active network")
            }
        } else {
            // Android 5.0-5.1 使用旧版 API
            updateNetworkStateLegacy(cm)
        }
    }

    /**
     * 使用旧版 API 获取网络状态（Android 5.0 兼容）
     */
    @Suppress("DEPRECATION")
    private fun updateNetworkStateLegacy(cm: ConnectivityManager) {
        val activeNetworkInfo: android.net.NetworkInfo? = cm.activeNetworkInfo

        if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
            val networkType = getNetworkTypeLegacy(activeNetworkInfo)
            _networkState.value = NetworkState.Connected(networkType)
        } else {
            _networkState.value = NetworkState.Disconnected("No active network")
        }
    }

    /**
     * 更新网络状态（API 21+）
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun updateNetworkState(network: Network, isAvailable: Boolean) {
        val cm = connectivityManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0+ 使用 NetworkCapabilities
            val capabilities = cm.getNetworkCapabilities(network)
            if (capabilities != null) {
                val networkType = getNetworkType(capabilities)
                _networkState.value = NetworkState.Connected(networkType)
            } else {
                _networkState.value = NetworkState.Disconnected("No network capabilities")
            }
        } else {
            // Android 5.0-5.1 使用旧版 NetworkInfo
            @Suppress("DEPRECATION")
            val networkInfo: android.net.NetworkInfo? = cm.getNetworkInfo(network)
            if (networkInfo != null && networkInfo.isConnected) {
                val networkType = getNetworkTypeLegacy(networkInfo)
                _networkState.value = NetworkState.Connected(networkType)
            } else {
                _networkState.value = NetworkState.Disconnected("No network info")
            }
        }
    }

    /**
     * 获取网络类型（API 21+，推荐方式）
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun getNetworkType(capabilities: NetworkCapabilities): NetworkType {
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                // Android 10+ (API 29+) 支持更详细的网络能力判断
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    getCellularNetworkType(capabilities)
                } else {
                    // Android 6.0-9.0 根据下行带宽估算
                    estimateCellularTypeByBandwidth(capabilities)
                }
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
            else -> NetworkType.UNKNOWN
        }
    }

    /**
     * 获取移动网络类型（Android 10+）
     */
    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getCellularNetworkType(capabilities: NetworkCapabilities): NetworkType {
        // Android 11+ (API 30+) 支持 5G 检测
        // 注意：使用反射动态获取 NET_CAPABILITY_MMSECURE 常量，Lint 无法识别，需抑制警告
        if (netCapabilityMmsecure > 0 && capabilities.hasCapability(netCapabilityMmsecure)) {
            return NetworkType.MOBILE_5G
        }

        // 其他情况根据下行带宽估算
        return estimateCellularTypeByBandwidth(capabilities)
    }

    /**
     * 根据带宽估算移动网络类型
     */
    private fun estimateCellularTypeByBandwidth(capabilities: NetworkCapabilities): NetworkType {
        val downSpeed = capabilities.linkDownstreamBandwidthKbps
        return when {
            downSpeed >= 100_000 -> NetworkType.MOBILE_5G  // >100Mbps
            downSpeed >= 10_000 -> NetworkType.MOBILE_4G   // >10Mbps
            downSpeed >= 1_000 -> NetworkType.MOBILE_3G    // >1Mbps
            else -> NetworkType.MOBILE_2G
        }
    }

    /**
     * 使用旧版 API 获取网络类型（Android 5.0 兼容）
     */
    @Suppress("DEPRECATION")
    private fun getNetworkTypeLegacy(networkInfo: android.net.NetworkInfo): NetworkType {
        return when (networkInfo.type) {
            ConnectivityManager.TYPE_WIFI -> NetworkType.WIFI
            ConnectivityManager.TYPE_MOBILE -> {
                // 根据子类型判断移动网络类型
                when (networkInfo.subtype) {
                    // 2G
                    in listOf(
                        1,   // GPRS
                        2,   // EDGE
                        4,   // CDMA
                        7,   // 1xRTT
                        16   // IDEN
                    ) -> NetworkType.MOBILE_2G

                    // 3G
                    in listOf(
                        3,   // UMTS
                        5,   // CDMA - EVDO rev. 0
                        6,   // CDMA - EVDO rev. A
                        8,   // HSDPA
                        9,   // HSUPA
                        10,  // HSPA
                        11,  // iDen
                        12,  // EVDO rev. B
                        14,  // eHRPD
                        15   // HSPA+
                    ) -> NetworkType.MOBILE_3G

                    // 4G
                    in listOf(
                        13,  // LTE
                        18   // LTE_CA
                    ) -> NetworkType.MOBILE_4G

                    // 5G (Android 9.0+)
                    20 -> NetworkType.MOBILE_5G

                    else -> NetworkType.MOBILE_2G
                }
            }
            ConnectivityManager.TYPE_ETHERNET -> NetworkType.ETHERNET
            else -> NetworkType.UNKNOWN
        }
    }

    override fun unregister() {
        networkCallback?.let {
            connectivityManager?.unregisterNetworkCallback(it)
        }
        networkCallback = null
    }

    override fun observeNetworkInfo(): Flow<NetworkInfo> {
        return networkState.map { state ->
            when (state) {
                is NetworkState.Connected -> {
                    val cm = connectivityManager ?: return@map NetworkInfo()
                    getNetworkInfoFromState(cm, state)
                }
                is NetworkState.Disconnected -> NetworkInfo(isConnected = false, isAvailable = false)
                else -> NetworkInfo()
            }
        }
    }

    /**
     * 获取网络详细信息
     */
    @Suppress("DEPRECATION")
    private fun getNetworkInfoFromState(
        cm: ConnectivityManager,
        state: NetworkState.Connected
    ): NetworkInfo {
        val networkInfo = NetworkInfo(
            type = state.type,
            isConnected = true,
            isAvailable = true
        )

        // Android 6.0+ 使用新 API
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = cm.activeNetwork
            if (activeNetwork != null) {
                val capabilities = cm.getNetworkCapabilities(activeNetwork)
                val legacyInfo: android.net.NetworkInfo? = cm.getNetworkInfo(activeNetwork)

                return networkInfo.copy(
                    isRoaming = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING) == false
                    } else {
                        legacyInfo?.isRoaming ?: false
                    },
                    typeName = legacyInfo?.typeName,
                    subtypeName = legacyInfo?.subtypeName
                )
            }
        } else {
            // Android 5.0-5.1 使用旧版 API
            val legacyInfo: android.net.NetworkInfo? = cm.activeNetworkInfo
            if (legacyInfo != null) {
                return networkInfo.copy(
                    isRoaming = legacyInfo.isRoaming,
                    typeName = legacyInfo.typeName,
                    subtypeName = legacyInfo.subtypeName
                )
            }
        }

        return networkInfo
    }

    override fun observeAvailability(): Flow<Boolean> {
        return networkState.map { state ->
            state.isConnected
        }
    }

    override fun getCurrentState(): NetworkState {
        return _networkState.value
    }

    override fun isConnected(): Boolean {
        return _networkState.value.isConnected
    }

    override fun isAvailable(): Boolean {
        val cm = connectivityManager ?: return false

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0+
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            // Android 5.0-5.1
            @Suppress("DEPRECATION")
            val networkInfo: android.net.NetworkInfo? = cm.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected
        }
    }
}