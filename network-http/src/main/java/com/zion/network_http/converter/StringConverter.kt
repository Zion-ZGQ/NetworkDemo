package com.zion.network_http.converter

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody

/**
 * 字符串转换器
 *
 * 用于将字符串和响应体之间进行转换
 */
class StringConverter : Converter {

    companion object {
        /**
         * 文本媒体类型
         */
        private val TEXT_MEDIA_TYPE = "text/plain; charset=utf-8".toMediaType()

        /**
         * 单例实例
         */
        val INSTANCE = StringConverter()
    }

    /**
     * 将字符串转换为请求体
     *
     * @param value 字符串值
     * @return 文本格式的请求体
     */
    override fun <T> toBody(value: T): RequestBody {
        return value.toString().toRequestBody(TEXT_MEDIA_TYPE)
    }

    /**
     * 将响应体转换为字符串
     *
     * @param body 响应体
     * @param type 目标类型（必须为 String 类型）
     * @return 字符串
     */
    override fun <T> fromBody(body: ResponseBody, type: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return body.string() as T
    }
}