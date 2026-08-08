package com.zion.network_core.api

/**
 * 网络请求结果密封类
 *
 * 用于封装所有网络请求的响应状态
 *
 * @param T 成功时的数据类型
 */
sealed class NetworkResult<out T> {

    /**
     * 成功状态
     *
     * @param data 返回的数据
     */
    data class Success<T>(val data: T) : NetworkResult<T>()

    /**
     * 错误状态
     *
     * @param exception 异常信息
     */
    data class Error<T>(val exception: Throwable) : NetworkResult<T>()

    /**
     * 加载中状态
     *
     * @param progress 进度百分比（0-100）
     */
    data class Loading<T>(val progress: Int = 0) : NetworkResult<T>()

    /**
     * 是否成功
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * 是否失败
     */
    val isError: Boolean
        get() = this is Error

    /**
     * 是否加载中
     */
    val isLoading: Boolean
        get() = this is Loading

    /**
     * 获取数据（如果成功）
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    /**
     * 获取数据，失败时抛出异常
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
        is Loading -> throw IllegalStateException("Still loading")
    }

    /**
     * 获取数据，失败时返回默认值
     */
    fun getOrDefault(default: @UnsafeVariance T): T = when (this) {
        is Success -> data
        else -> default
    }

    /**
     * 转换数据
     */
    inline fun <R> map(transform: (T) -> R): NetworkResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> Error(exception)
        is Loading -> Loading(progress)
    }

    /**
     * 成功时执行
     */
    inline fun onSuccess(action: (T) -> Unit): NetworkResult<T> {
        if (this is Success) action(data)
        return this
    }

    /**
     * 失败时执行
     */
    inline fun onError(action: (Throwable) -> Unit): NetworkResult<T> {
        if (this is Error) action(exception)
        return this
    }

    /**
     * 加载时执行
     */
    inline fun onLoading(action: (Int) -> Unit): NetworkResult<T> {
        if (this is Loading) action(progress)
        return this
    }
}