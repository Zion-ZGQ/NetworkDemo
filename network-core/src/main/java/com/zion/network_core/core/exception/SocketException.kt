package com.zion.network_core.core.exception

/**
 * Socket 相关异常
 *
 * @param code 错误码
 * @param message 错误信息
 * @param cause 原始异常
 */
class SocketException(
    code: Int = CODE_SOCKET_ERROR,
    message: String = "Socket connection failed",
    cause: Throwable? = null
) : NetworkException(code, message, cause) {

    companion object {
        const val CODE_SOCKET_ERROR = 3000
        const val CODE_SOCKET_TIMEOUT = 3001
        const val CODE_SOCKET_CLOSED = 3002
        const val CODE_SOCKET_CONNECT_FAILED = 3003
        const val CODE_SOCKET_SEND_FAILED = 3004
        const val CODE_SOCKET_RECEIVE_FAILED = 3005
    }
}