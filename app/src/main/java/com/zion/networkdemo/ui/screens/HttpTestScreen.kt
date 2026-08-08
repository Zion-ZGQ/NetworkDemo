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
import com.zion.network_core.api.NetworkResult
import com.zion.network_http.api.HttpRequest
import com.zion.network_http.core.RealHttpClient
import com.zion.network_http.extension.executeFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * HTTP 测试页面
 */
@Composable
fun HttpTestScreen(
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    // 响应结果
    var responseResult by remember { mutableStateOf<String?>(null) }

    // 测试日志
    var testLogs by remember { mutableStateOf(listOf<String>()) }

    // 加载状态
    var isLoading by remember { mutableStateOf(false) }

    // 添加日志
    fun addLog(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        testLogs = testLogs + "[$timestamp] $message"
    }

    // 清空日志
    fun clearLogs() {
        testLogs = emptyList()
        responseResult = null
    }

    // 测试 GET 请求
    fun testGetRequest() {
        scope.launch {
            try {
                isLoading = true
                addLog("开始 GET 请求: https://jsonplaceholder.typicode.com/posts/1")

                val client = RealHttpClient.getInstance()
                val request = HttpRequest.Builder()
                    .url("https://jsonplaceholder.typicode.com/posts/1")
                    .get()
                    .addHeader("Accept", "application/json")
                    .build()

                client.executeFlow<Post>(request)
                    .onEach { result ->
                        when (result) {
                            is NetworkResult.Loading -> {
                                addLog("加载中...")
                            }
                            is NetworkResult.Success -> {
                                isLoading = false
                                val response = result.data
                                responseResult = """
                                    状态码: ${response.code}
                                    消息: ${response.message}
                                    
                                    响应体:
                                    ${response.body}
                                """.trimIndent()
                                addLog("✅ 请求成功: ${response.code}")
                            }
                            is NetworkResult.Error -> {
                                isLoading = false
                                addLog("❌ 请求失败: ${result.exception.message}")
                            }
                        }
                    }
                    .catch { e ->
                        isLoading = false
                        addLog("❌ 异常: ${e.message}")
                    }
                    .collect()

            } catch (e: Exception) {
                isLoading = false
                addLog("❌ 错误: ${e.message}")
            }
        }
    }

    // 测试 POST 请求
    fun testPostRequest() {
        scope.launch {
            try {
                isLoading = true
                addLog("开始 POST 请求: https://jsonplaceholder.typicode.com/posts")

                val client = RealHttpClient.getInstance()
                val request = HttpRequest.Builder()
                    .url("https://jsonplaceholder.typicode.com/posts")
                    .postJson("{\"title\":\"Test\",\"body\":\"Test body\",\"userId\":1}")
                    .addHeader("Content-Type", "application/json")
                    .build()

                client.executeFlow<Post>(request)
                    .onEach { result ->
                        when (result) {
                            is NetworkResult.Loading -> {
                                addLog("加载中...")
                            }
                            is NetworkResult.Success -> {
                                isLoading = false
                                val response = result.data
                                responseResult = """
                                    状态码: ${response.code}
                                    消息: ${response.message}
                                    
                                    响应体:
                                    ${response.body}
                                """.trimIndent()
                                addLog("✅ 请求成功: ${response.code}")
                            }
                            is NetworkResult.Error -> {
                                isLoading = false
                                addLog("❌ 请求失败: ${result.exception.message}")
                            }
                        }
                    }
                    .catch { e ->
                        isLoading = false
                        addLog("❌ 异常: ${e.message}")
                    }
                    .collect()

            } catch (e: Exception) {
                isLoading = false
                addLog("❌ 错误: ${e.message}")
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 标题
        Text(
            text = "HTTP 测试",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        // 功能说明
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "功能说明",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• 支持标准 GET/POST 请求\n• 自动 JSON 解析\n• 完整的错误处理\n• 响应式 Flow API",
                    fontSize = 14.sp
                )
            }
        }

        // 测试按钮
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "测试功能",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { testGetRequest() },
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("GET 请求")
                        }
                    }

                    Button(
                        onClick = { testPostRequest() },
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("POST 请求")
                        }
                    }
                }

                Button(
                    onClick = { clearLogs() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("清空日志")
                }
            }
        }

        // 响应结果
        if (responseResult != null) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "响应结果",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = responseResult!!,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp)
                    )
                }
            }
        }

        // 测试日志
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "测试日志",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (testLogs.isEmpty()) {
                    Text(
                        text = "暂无日志",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                            .verticalScroll(rememberScrollState())
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        testLogs.forEach { log ->
                            Text(
                                text = log,
                                fontSize = 12.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 测试用的数据类
 */
@Suppress("unused")
data class Post(
    val userId: Int = 0,
    val id: Int = 0,
    val title: String = "",
    val body: String = ""
)