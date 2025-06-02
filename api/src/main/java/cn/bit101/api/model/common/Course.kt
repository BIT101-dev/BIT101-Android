package cn.bit101.api.model.common

import cn.bit101.api.model.UniqueData
import java.io.Serializable

data class Course(
    val commentNum: Int,
    override val id: Int,
    val likeNum: Int,
    val name: String,
    val number: String,
    val rate: Int,
    val teachersName: String,
    val teachersNumber: String
) : Serializable, UniqueData