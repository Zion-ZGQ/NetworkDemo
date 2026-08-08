package com.zion.networkdemo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zion.networkdemo.ui.navigation.Screen

/**
 * 首页（功能入口）
 */
@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 标题
        Text(
            text = "NetworkKit 测试",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // 说明
        Text(
            text = "通用 Android 网络工具库，支持 HTTP、TCP、WebSocket、下载等功能。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // 功能入口
        Text(
            text = "功能模块",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // 模块按钮列表
        ModuleButton(
            icon = Icons.Filled.Wifi,
            title = "网络状态检测",
            description = "实时监听网络状态变化，识别网络类型",
            onClick = { navController.navigate(Screen.NetworkMonitor.route) }
        )

        ModuleButton(
            icon = Icons.Filled.Cloud,
            title = "HTTP 模块",
            description = "HTTP/HTTPS 请求测试",
            onClick = { navController.navigate(Screen.Http.route) }
        )

        ModuleButton(
            icon = Icons.Filled.Settings,
            title = "TCP Socket 模块",
            description = "TCP Socket 连接测试",
            onClick = { navController.navigate(Screen.TcpSocket.route) }
        )

        ModuleButton(
            icon = Icons.Filled.Language,
            title = "WebSocket 模块",
            description = "WebSocket 连接测试",
            onClick = { navController.navigate(Screen.WebSocket.route) }
        )

        ModuleButton(
            icon = Icons.Filled.Download,
            title = "下载测试模块",
            description = "文件下载、断点续传测试",
            onClick = { navController.navigate(Screen.Download.route) }
        )
    }
}

/**
 * 模块按钮组件
 */
@Composable
fun ModuleButton(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 图标
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            // 文本信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 箭头
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = "进入",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}