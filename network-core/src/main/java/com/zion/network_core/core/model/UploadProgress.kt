package com.zion.network_core.core.model

/**
 * 上传进度模型
 *
 * @param url 上传地址
 * @param fileName 文件名
 * @param currentBytes 已上传字节数
 * @param totalBytes 总字节数
 * @param progress 进度百分比（0-100）
 * @param speed 上传速度（字节/秒）
 */
data class UploadProgress(
    val url: String,
    val fileName: String,
    val currentBytes: Long = 0,
    val totalBytes: Long = 0,
    val progress: Int = 0,
    val speed: Long = 0
) {
    /**
     * 是否完成
     */
    val isCompleted: Boolean
        get() = progress >= 100

    /**
     * 格式化进度
     */
    fun formatProgress(): String = "${progress}%"

    /**
     * 格式化速度
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
}