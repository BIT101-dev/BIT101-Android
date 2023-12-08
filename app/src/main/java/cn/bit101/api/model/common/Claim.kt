package cn.bit101.api.model.common

import java.io.Serializable

data class Claim(
    val id: Int,
    val text: String
) : Serializable