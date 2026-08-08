package com.zion.network_core.core.exception

/**
 * 下载相关异常
 *
 * @param code 错误码
 * @param message 错误信息
 * @param cause 原始异常
 */
class DownloadException(
    code: Int = CODE_DOWNLOAD_ERROR,
    message: String = "Download failed",
    cause: Throwable? = null
) : NetworkException(code, message, cause) {

    companion object {
        const val CODE_DOWNLOAD_ERROR = 4000
        const val CODE_DOWNLOAD_PAUSED = 4001
        const val CODE_DOWNLOAD_CANCELLED = 4002
        const val CODE_DOWNLOAD_DISK_ERROR = 4003
        const val CODE_DOWNLOAD_NETWORK_ERROR = 4004
        const val CODE_DOWNLOAD_FILE_NOT_FOUND = 4005
    }
}