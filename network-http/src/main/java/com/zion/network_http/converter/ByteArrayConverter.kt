package com.zion.network_http.converter

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody

/**
 * 字节数组转换器
 *
 * 用于将字节数组和响应体之间进行转换
 */
class ByteArrayConverter : Converter {

    companion object {
        /**
         * 二进制媒体类型
         */
        private val BINARY_MEDIA_TYPE = "application/octet-stream".toMediaType()

        /**
         * 单例实例
         */
        val INSTANCE = ByteArrayConverter()
    }

    /**
     * 将字节数组转换为请求体
     *
     * @param value 字节数组（必须为 ByteArray 类型）
     * @return 二进制格式的请求体
     */
    override fun <T> toBody(value: T): RequestBody {
        @Suppress("UNCHECKED_CAST")
        return (value as ByteArray).toRequestBody(BINARY_MEDIA_TYPE)
    }

    /**
     * 将响应体转换为字节数组
     *
     * @param body 响应体
     * @param type 目标类型（必须为 ByteArray 类型）
     * @return 字节数组
     */
    override fun <T> fromBody(body: ResponseBody, type: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return body.bytes() as T
    }
}