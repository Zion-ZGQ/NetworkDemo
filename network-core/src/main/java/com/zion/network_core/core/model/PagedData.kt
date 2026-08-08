package com.zion.network_core.core.model

/**
 * 分页数据模型
 *
 * @param T 数据类型
 * @param data 数据列表
 * @param page 当前页码
 * @param pageSize 每页大小
 * @param total 总数
 * @param hasNext 是否有下一页
 */
data class PagedData<T>(
    val data: List<T>,
    val page: Int,
    val pageSize: Int,
    val total: Int,
    val hasNext: Boolean
) {
    /**
     * 是否为空
     */
    val isEmpty: Boolean
        get() = data.isEmpty()

    /**
     * 总页数
     */
    val totalPage: Int
        get() = if (pageSize > 0) (total + pageSize - 1) / pageSize else 0
}