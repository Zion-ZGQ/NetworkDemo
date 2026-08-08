package com.zion.network_http.converter

import okhttp3.RequestBody
import okhttp3.ResponseBody

/**
 * 数据转换器接口
 *
 * 用于将请求和响应数据在对象和字节数组之间进行转换
 *
 * @see GsonConverter
 * @see StringConverter
 * @see ByteArrayConverter
 */
interface Converter {

    /**
     * 将对象转换为请求体
     *
     * @param value 要转换的对象
     * @return 请求体
     */
    fun <T> toBody(value: T): RequestBody

    /**
     * 将响应体转换为对象
     *
     * @param body 响应体
     * @param type 目标类型
     * @return 转换后的对象
     */
    fun <T> fromBody(body: ResponseBody, type: Class<T>): T
}

/**
 * 内联扩展函数：将响应体转换为对象（支持泛型类型擦除）
 *
 * @param body 响应体
 * @return 转换后的对象
 */
inline fun <reified T> Converter.fromBody(body: ResponseBody): T {
    return fromBody(body, T::class.java)
}