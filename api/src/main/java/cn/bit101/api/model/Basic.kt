package cn.bit101.api.model

/**
 * 用于标记数据唯一性的接口，防止服务端发送相同的数据项让 LazyList 崩溃
 */
interface UniqueData {
    val id: Comparable<*>
}