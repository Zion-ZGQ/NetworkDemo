package com.zion.networkdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zion.network_core.api.NetworkKit
import com.zion.network_core.core.logger.LogLevel
import com.zion.network_monitor.NetworkMonitorProvider
import com.zion.networkdemo.ui.navigation.Screen
import com.zion.networkdemo.ui.screens.EmptyScreen
import com.zion.networkdemo.ui.screens.HomeScreen
import com.zion.networkdemo.ui.screens.HttpTestScreen
import com.zion.networkdemo.ui.screens.NetworkMonitorScreen
import com.zion.networkdemo.ui.theme.NetworkDemoTheme

/**
 * 主 Activity
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化 NetworkKit
        NetworkKit.init(this) {
            setLogLevel(LogLevel.DEBUG)
            setDebug(true)
            setConnectTimeout(30_000L)
        }

        // 注册网络状态监听
        NetworkMonitorProvider.register()

        enableEdgeToEdge()
        setContent {
            NetworkDemoTheme {
                NetworkDemoApp()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 注销网络监听
        NetworkMonitorProvider.unregister()
    }
}

/**
 * 应用主容器
 */
@Composable
fun NetworkDemoApp() {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        AppNavigation(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

/**
 * 应用导航
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        // 首页
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        // 网络状态检测
        composable(Screen.NetworkMonitor.route) {
            NetworkMonitorScreen()
        }

        // HTTP 模块
        composable(Screen.Http.route) {
            HttpTestScreen()
        }

        // TCP Socket 模块（未实现）
        composable(Screen.TcpSocket.route) {
            EmptyScreen(title = Screen.TcpSocket.title)
        }

        // WebSocket 模块（未实现）
        composable(Screen.WebSocket.route) {
            EmptyScreen(title = Screen.WebSocket.title)
        }

        // 下载测试模块（未实现）
        composable(Screen.Download.route) {
            EmptyScreen(title = Screen.Download.title)
        }
    }
}