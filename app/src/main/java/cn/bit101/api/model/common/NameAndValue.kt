package cn.bit101.api.model.common

data class NameAndValue<T>(
    val name: String,
    val value: T,
)