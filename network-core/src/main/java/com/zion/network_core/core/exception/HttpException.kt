package com.zion.network_core.core.exception

/**
 * HTTP 相关异常
 *
 * @param httpCode HTTP 状态码
 * @param message 错误信息
 * @param cause 原始异常
 */
class HttpException(
    val httpCode: Int,
    message: String = "HTTP request failed",
    cause: Throwable? = null
) : NetworkException(
    code = CODE_HTTP_ERROR,
    message = message,
    cause = cause
) {
    companion object {
        const val CODE_HTTP_ERROR = 2000
        const val CODE_HTTP_400 = 2400
        const val CODE_HTTP_401 = 2401
        const val CODE_HTTP_403 = 2403
        const val CODE_HTTP_404 = 2404
        const val CODE_HTTP_500 = 2500
    }

    /**
     * 是否为客户端错误（4xx）
     */
    fun isClientError(): Boolean = httpCode in 400..499

    /**
     * 是否为服务器错误（5xx）
     */
    fun isServerError(): Boolean = httpCode in 500..599
}