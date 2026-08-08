package com.zion.networkdemo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zion.network_core.api.NetworkKit
import com.zion.network_core.core.model.NetworkState
import com.zion.network_core.core.model.NetworkType
import com.zion.network_monitor.NetworkInfo
import com.zion.network_monitor.NetworkMonitorProvider
import kotlinx.coroutines.launch

/**
 * 网络状态检测页面
 */
@Composable
fun NetworkMonitorScreen(
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    // 监听网络状态
    val networkState by NetworkMonitorProvider.monitor.networkState.collectAsState()

    // 网络详细信息
    var networkInfo by remember { mutableStateOf<NetworkInfo?>(null) }

    // 测试日志
    var testLogs by remember { mutableStateOf(listOf<String>()) }

    // 启动网络信息监听
    LaunchedEffect(Unit) {
        NetworkMonitorProvider.monitor.observeNetworkInfo().collect { info ->
            networkInfo = info
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 标题
        Text(
            text = "网络状态检测",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // 网络状态卡片
        NetworkStateCard(networkState)

        // 网络详细信息卡片
        networkInfo?.let { info ->
            NetworkInfoCard(info)
        }

        // 功能测试按钮
        TestButtons(
            onTestNetworkState = {
                scope.launch {
                    val state = NetworkMonitorProvider.monitor.getCurrentState()
                    testLogs = addLog(testLogs, "当前状态: $state")
                }
            },
            onTestConnection = {
                scope.launch {
                    val isConnected = NetworkMonitorProvider.monitor.isConnected()
                    val isAvailable = NetworkMonitorProvider.monitor.isAvailable()
                    testLogs = addLog(testLogs, "连接: $isConnected, 可用: $isAvailable")
                }
            },
            onTestLog = {
                NetworkKit.logger.d("Test", "这是一条测试日志")
                testLogs = addLog(testLogs, "日志已输出到 Logcat")
            },
            onClearLogs = {
                testLogs = emptyList()
            }
        )

        // 测试日志
        TestLogsCard(testLogs)
    }
}

@Composable
fun NetworkStateCard(networkState: NetworkState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "网络状态",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 状态图标
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = when (networkState) {
                                is NetworkState.Connected -> Color(0xFF4CAF50)
                                is NetworkState.Disconnected -> Color(0xFFF44336)
                                else -> Color.Gray
                            },
                            shape = RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (networkState) {
                            is NetworkState.Connected -> "✓"
                            is NetworkState.Disconnected -> "✗"
                            NetworkState.Idle -> "-"
                            NetworkState.Connecting -> "⏳"
                        },
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 状态文本
                Column {
                    Text(
                        text = when (networkState) {
                            is NetworkState.Connected -> "已连接"
                            is NetworkState.Disconnected -> "已断开"
                            NetworkState.Idle -> "空闲"
                            NetworkState.Connecting -> "连接中"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = when (networkState) {
                            is NetworkState.Connected -> Color(0xFF4CAF50)
                            is NetworkState.Disconnected -> Color(0xFFF44336)
                            else -> Color.Gray
                        }
                    )

                    if (networkState is NetworkState.Connected) {
                        Text(
                            text = getNetworkTypeText(networkState.type),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NetworkInfoCard(info: NetworkInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "网络详细信息",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider()

            InfoRow("网络类型", info.typeDescription)
            InfoRow("是否连接", if (info.isConnected) "是" else "否")
            InfoRow("是否可用", if (info.isAvailable) "是" else "否")
            InfoRow("是否漫游", if (info.isRoaming) "是" else "否")
            InfoRow("类型名称", info.typeName ?: "未知")
            InfoRow("子类型", info.subtypeName ?: "未知")

            // 网络类型标签
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (info.isWifi) {
                    SuggestionChip(
                        onClick = { },
                        label = { Text("WiFi") },
                        icon = {
                            Text("📶", fontSize = 16.sp)
                        }
                    )
                }
                if (info.isMobile) {
                    SuggestionChip(
                        onClick = { },
                        label = { Text("移动网络") },
                        icon = {
                            Text("📱", fontSize = 16.sp)
                        }
                    )
                }
                if (info.is5G) {
                    SuggestionChip(
                        onClick = { },
                        label = { Text("5G") },
                        icon = {
                            Text("🚀", fontSize = 16.sp)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TestButtons(
    onTestNetworkState: () -> Unit,
    onTestConnection: () -> Unit,
    onTestLog: () -> Unit,
    onClearLogs: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "功能测试",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider()

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onTestNetworkState,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("测试状态", fontSize = 12.sp)
                }

                Button(
                    onClick = onTestConnection,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("测试连接", fontSize = 12.sp)
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onTestLog,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("测试日志", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onClearLogs,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("清空日志", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun TestLogsCard(logs: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "测试日志",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider()

            if (logs.isEmpty()) {
                Text(
                    text = "暂无日志",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            } else {
                logs.forEach { log ->
                    Text(
                        text = log,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

fun getNetworkTypeText(type: NetworkType): String {
    return when (type) {
        NetworkType.WIFI -> "WiFi 网络"
        NetworkType.MOBILE_2G -> "2G 移动网络"
        NetworkType.MOBILE_3G -> "3G 移动网络"
        NetworkType.MOBILE_4G -> "4G 移动网络"
        NetworkType.MOBILE_5G -> "5G 移动网络"
        NetworkType.ETHERNET -> "以太网"
        NetworkType.UNKNOWN -> "未知网络"
    }
}

fun addLog(logs: List<String>, message: String): List<String> {
    val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        .format(java.util.Date())
    return logs + "[$timestamp] $message"
}