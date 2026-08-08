package com.zion.network_http.converter

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import java.lang.reflect.Type

/**
 * Gson 数据转换器
 *
 * 使用 Gson 库将对象和 JSON 字符串之间进行转换
 *
 * @param gson Gson 实例，默认使用标准配置
 */
class GsonConverter(
    private val gson: Gson = GsonBuilder()
        .setLenient()
        .serializeNulls()
        .create()
) : Converter {

    companion object {
        /**
         * JSON 媒体类型
         */
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    /**
     * 将对象转换为 JSON 格式的请求体
     *
     * @param value 要转换的对象
     * @return JSON 格式的请求体
     */
    override fun <T> toBody(value: T): RequestBody {
        val json = gson.toJson(value)
        return json.toRequestBody(JSON_MEDIA_TYPE)
    }

    /**
     * 将 JSON 格式的响应体转换为对象
     *
     * @param body 响应体
     * @param type 目标类型
     * @return 转换后的对象
     */
    override fun <T> fromBody(body: ResponseBody, type: Class<T>): T {
        val jsonString = body.string()
        return gson.fromJson(jsonString, type)
    }

    /**
     * 将 JSON 格式的响应体转换为对象（支持泛型类型）
     *
     * @param body 响应体
     * @param type 目标类型
     * @return 转换后的对象
     */
    fun <T> fromBody(body: ResponseBody, type: Type): T {
        val jsonString = body.string()
        return gson.fromJson(jsonString, type)
    }

    /**
     * 将 JSON 格式的响应体转换为对象（使用 TypeToken）
     *
     * @param body 响应体
     * @param typeToken 类型标记
     * @return 转换后的对象
     */
    fun <T> fromBody(body: ResponseBody, typeToken: TypeToken<T>): T {
        return fromBody(body, typeToken.type)
    }
}