package cn.bit101.api.model.common

import java.io.Serializable

// 身份
data class Identity(
    // 身份唯一编码
    val id: Int,

    // 勾勾颜色，为空则不显示勾勾
    val color: String,

    // 身份称号
    val text: String,
) : Serializable