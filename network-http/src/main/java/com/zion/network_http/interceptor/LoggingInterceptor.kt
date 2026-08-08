package com.zion.network_http.interceptor

import com.zion.network_core.api.NetworkKit
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

/**
 * 日志拦截器
 *
 * 用于记录 HTTP 请求和响应的详细信息，使用 NetworkKit.logger 进行日志输出
 *
 * @param level 日志级别（NONE, BASIC, HEADERS, BODY）
 */
class LoggingInterceptor(
    private val level: Level = Level.BASIC
) : Interceptor {

    companion object {
        private const val TAG = "HttpLogger"
    }

    /**
     * 日志级别枚举
     */
    enum class Level {
        /**
         * 不记录日志
         */
        NONE,

        /**
         * 基础信息（请求方法、URL、响应码、响应时间）
         */
        BASIC,

        /**
         * 基础信息 + 请求头和响应头
         */
        HEADERS,

        /**
         * 基础信息 + 请求头和响应头 + 请求体和响应体
         */
        BODY
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val logger = NetworkKit.logger

        // 如果日志级别为 NONE，直接执行请求
        if (level == Level.NONE) {
            return chain.proceed(request)
        }

        // 记录请求信息
        val startNs = System.nanoTime()
        logRequest(request, logger)

        // 执行请求
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            logger.e(TAG, "Request failed: ${request.url}", e)
            throw e
        }

        // 记录响应信息
        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
        logResponse(response, logger, tookMs)

        return response
    }

    /**
     * 记录请求信息
     */
    private fun logRequest(request: Request, logger: com.zion.network_core.core.logger.Logger) {
        val sb = StringBuilder()
        sb.append("\n┌────── Request ────────────────────────────────────────\n")
        sb.append("│ ${request.method} ${request.url}\n")

        if (level == Level.HEADERS || level == Level.BODY) {
            val headers = request.headers
            for (i in 0 until headers.size) {
                sb.append("│ ${headers.name(i)}: ${headers.value(i)}\n")
            }
        }

        if (level == Level.BODY && request.body != null) {
            val body = request.body!!
            val buffer = Buffer()
            body.writeTo(buffer)

            var charset: Charset = StandardCharsets.UTF_8
            val contentType = body.contentType()
            if (contentType != null) {
                charset = contentType.charset(StandardCharsets.UTF_8)!!
            }

            // 尝试读取为 UTF-8 文本
            try {
                val bodyString = buffer.clone().readString(charset)
                sb.append("│ \n")
                sb.append("│ $bodyString\n")
            } catch (e: Exception) {
                // 如果读取失败，说明是二进制数据
                sb.append("│ (binary ${body.contentLength()}-byte body)\n")
            }
        }

        sb.append("└───────────────────────────────────────────────────────")
        logger.d(TAG, sb.toString())
    }

    /**
     * 记录响应信息
     */
    private fun logResponse(
        response: Response,
        logger: com.zion.network_core.core.logger.Logger,
        tookMs: Long
    ) {
        val sb = StringBuilder()
        sb.append("\n┌────── Response ───────────────────────────────────────\n")
        sb.append("│ ${response.code} ${response.message} (${tookMs}ms)\n")

        if (level == Level.HEADERS || level == Level.BODY) {
            val headers = response.headers
            for (i in 0 until headers.size) {
                sb.append("│ ${headers.name(i)}: ${headers.value(i)}\n")
            }
        }

        if (level == Level.BODY) {
            val responseBody = response.body
            if (responseBody != null) {
                val source = responseBody.source()
                source.request(Long.MAX_VALUE)
                var buffer = source.buffer

                var charset: Charset = StandardCharsets.UTF_8
                val contentType = responseBody.contentType()
                if (contentType != null) {
                    charset = contentType.charset(StandardCharsets.UTF_8)!!
                }

                // 尝试读取为 UTF-8 文本
                try {
                    val bodyString = buffer.clone().readString(charset)
                    sb.append("│ \n")
                    // 限制日志长度，避免打印过大的响应体
                    if (bodyString.length > 4096) {
                        sb.append("│ ${bodyString.substring(0, 4096)}...\n")
                        sb.append("│ (Response body too large, truncated)\n")
                    } else {
                        sb.append("│ $bodyString\n")
                    }
                } catch (e: Exception) {
                    // 如果读取失败，说明是二进制数据
                    sb.append("│ (binary ${responseBody.contentLength()}-byte body)\n")
                }
            }
        }

        sb.append("└───────────────────────────────────────────────────────")
        logger.d(TAG, sb.toString())
    }
}