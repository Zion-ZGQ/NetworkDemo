package com.zion.networkdemo.ui.navigation

/**
 * 应用导航路由定义
 */
sealed class Screen(val route: String, val title: String) {
    /**
     * 首页（功能入口）
     */
    data object Home : Screen("home", "NetworkKit 测试")

    /**
     * 网络状态检测
     */
    data object NetworkMonitor : Screen("network_monitor", "网络状态检测")

    /**
     * HTTP 模块
     */
    data object Http : Screen("http", "HTTP 模块")

    /**
     * TCP Socket 模块
     */
    data object TcpSocket : Screen("tcp_socket", "TCP Socket 模块")

    /**
     * WebSocket 模块
     */
    data object WebSocket : Screen("web_socket", "WebSocket 模块")

    /**
     * 下载测试模块
     */
    data object Download : Screen("download", "下载测试模块")
}