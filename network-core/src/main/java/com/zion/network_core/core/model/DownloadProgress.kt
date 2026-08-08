package com.zion.network_core.core.model

/**
 * 下载进度模型
 *
 * @param url 下载地址
 * @param fileName 文件名
 * @param savePath 保存路径
 * @param currentBytes 已下载字节数
 * @param totalBytes 总字节数
 * @param progress 进度百分比（0-100）
 * @param speed 下载速度（字节/秒）
 * @param status 下载状态
 */
data class DownloadProgress(
    val url: String,
    val fileName: String,
    val savePath: String,
    val currentBytes: Long = 0,
    val totalBytes: Long = 0,
    val progress: Int = 0,
    val speed: Long = 0,
    val status: DownloadStatus = DownloadStatus.PENDING
) {
    /**
     * 是否完成
     */
    val isCompleted: Boolean
        get() = status == DownloadStatus.COMPLETED

    /**
     * 是否失败
     */
    val isFailed: Boolean
        get() = status == DownloadStatus.FAILED

    /**
     * 是否正在下载
     */
    val isDownloading: Boolean
        get() = status == DownloadStatus.DOWNLOADING

    /**
     * 格式化进度（如 "50%"）
     */
    fun formatProgress(): String = "${progress}%"

    /**
     * 格式化速度（如 "1.5 MB/s"）
     */
    fun formatSpeed(): String {
        if (speed <= 0) return "0 B/s"

        val units = arrayOf("B/s", "KB/s", "MB/s", "GB/s")
        var value = speed.toDouble()
        var unitIndex = 0

        while (value >= 1024 && unitIndex < units.lastIndex) {
            value /= 1024
            unitIndex++
        }

        return "${String.format("%.1f", value)} ${units[unitIndex]}"
    }

    /**
     * 格式化文件大小
     */
    fun formatSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"

        val units = arrayOf("B", "KB", "MB", "GB")
        var value = bytes.toDouble()
        var unitIndex = 0

        while (value >= 1024 && unitIndex < units.lastIndex) {
            value /= 1024
            unitIndex++
        }

        return "${String.format("%.1f", value)} ${units[unitIndex]}"
    }
}

/**
 * 下载状态枚举
 */
enum class DownloadStatus {
    /**
     * 等待中
     */
    PENDING,

    /**
     * 下载中
     */
    DOWNLOADING,

    /**
     * 已暂停
     */
    PAUSED,

    /**
     * 已完成
     */
    COMPLETED,

    /**
     * 已失败
     */
    FAILED,

    /**
     * 已取消
     */
    CANCELLED
}