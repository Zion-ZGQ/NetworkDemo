package com.zion.network_core.core.exception

/**
 * 网络库基础异常类
 *
 * 所有网络相关的异常都应继承此类
 *
 * @param code 错误码
 * @param message 错误信息
 * @param cause 原始异常
 */
open class NetworkException(
    val code: Int,
    override val message: String,
    override val cause: Throwable? = null
) : RuntimeException(message, cause) {

    companion object {
        // 通用错误码
        const val CODE_UNKNOWN = -1
        const val CODE_NETWORK_UNAVAILABLE = 1000
        const val CODE_TIMEOUT = 1001
        const val CODE_CANCELLED = 1002
        const val CODE_INVALID_PARAMS = 1003
    }

    override fun toString(): String {
        return "${this::class.simpleName}(code=$code, message=$message)"
    }
}