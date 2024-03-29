package cn.bit101.api.model.common

import cn.bit101.api.model.UniqueData
import java.io.Serializable

data class Image (
    // 图片唯一编码
    val mid: String,

    // 原图链接
    val url: String,

    // 低分辨率图片链接
    val lowUrl: String,
) : Serializable, UniqueData {
    override val id = mid
}

// 头像
typealias Avatar = Image