package com.zion.network_http.api

import com.zion.network_core.api.NetworkResult
import com.zion.network_http.config.HttpConfig
import kotlinx.coroutines.flow.Flow

/**
 * HTTP 客户端接口
 *
 * 定义了 HTTP 客户端的基本操作，包括请求执行、配置管理等功能
 *
 * 使用示例：
 * ```kotlin
 * val client = RealHttpClient.getInstance(config)
 * val response = client.execute(request, User::class.java)
 * ```
 */
interface HttpClient {

    /**
     * 同步执行请求
     *
     * @param request HTTP 请求
     * @param type 响应类型
     * @return HTTP 响应
     */
    fun <T> execute(request: HttpRequest, type: Class<T>): NetworkResult<HttpResponse<T>>

    /**
     * 异步执行请求（挂起函数）
     *
     * @param request HTTP 请求
     * @param type 响应类型
     * @return HTTP 响应
     */
    suspend fun <T> executeSuspend(request: HttpRequest, type: Class<T>): NetworkResult<HttpResponse<T>>

    /**
     * 执行请求并返回 Flow
     *
     * @param request HTTP 请求
     * @param type 响应类型
     * @return Flow<NetworkResult<HttpResponse<T>>>
     */
    fun <T> executeFlow(request: HttpRequest, type: Class<T>): Flow<NetworkResult<HttpResponse<T>>>

    /**
     * 关闭客户端
     *
     * 释放所有资源，关闭连接池
     */
    fun close()

    /**
     * 获取客户端配置
     *
     * @return HTTP 配置
     */
    fun getConfig(): HttpConfig

    /**
     * 取消所有请求
     */
    fun cancelAll()

    /**
     * 取消指定标签的请求
     *
     * @param tag 请求标签
     */
    fun cancelByTag(tag: String)
}